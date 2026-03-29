package com.notes.notesmarketplace.service.impl;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.support.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void storePendingPayment_shouldStoreAndRetrieveValues() {
        paymentService.storePendingPayment("T1", 10L, 20L);

        assertThat(paymentService.getBuyerId("T1")).isEqualTo(10L);
        assertThat(paymentService.getNoteId("T1")).isEqualTo(20L);
    }

    @Test
    void getPendingPayment_shouldReturnNull_whenTransactionDoesNotExist() {
        assertThat(paymentService.getBuyerId("missing")).isNull();
        assertThat(paymentService.getNoteId("missing")).isNull();
    }

    @Test
    void removePendingPayment_shouldRemoveStoredTransaction() {
        paymentService.storePendingPayment("T2", 11L, 21L);

        paymentService.removePendingPayment("T2");

        assertThat(paymentService.getBuyerId("T2")).isNull();
        assertThat(paymentService.getNoteId("T2")).isNull();
    }

    @Test
    void createPaymentSession_shouldThrowWrappedError_whenUserIsMissing() {
        ReflectionTestUtils.setField(paymentService, "storeId", "store");
        ReflectionTestUtils.setField(paymentService, "storePassword", "password");
        ReflectionTestUtils.setField(paymentService, "sslUrl", "https://example.com");

        when(userRepository.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPaymentSession("missing@mail.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment initialization failed");
    }

    @Test
    void createPaymentSession_shouldThrowWrappedError_whenNoteIsMissing() {
        ReflectionTestUtils.setField(paymentService, "storeId", "store");
        ReflectionTestUtils.setField(paymentService, "storePassword", "password");
        ReflectionTestUtils.setField(paymentService, "sslUrl", "https://example.com");

        User user = TestDataBuilder.user(3L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        when(userRepository.findByEmail("buyer@mail.com")).thenReturn(Optional.of(user));
        when(noteRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPaymentSession("buyer@mail.com", 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment initialization failed");
    }

    @Test
    void initiatePayment_success() {
        ReflectionTestUtils.setField(paymentService, "storeId", "store");
        ReflectionTestUtils.setField(paymentService, "storePassword", "password");
        ReflectionTestUtils.setField(paymentService, "sslUrl", "https://example.com");

        User user = TestDataBuilder.user(10L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        Note note = TestDataBuilder.note(55L, "Gateway Test Note", null);
        note.setPrice(149.0);

        when(userRepository.findByEmail("buyer@mail.com")).thenReturn(Optional.of(user));
        when(noteRepository.findById(55L)).thenReturn(Optional.of(note));

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            Map<String, Object> body = Map.of("GatewayPageURL", "https://gateway.url/session/ok");
            ResponseEntity<Map<String, Object>> response = ResponseEntity.ok(body);

            when(mock.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                    .thenReturn((ResponseEntity) response);
        })) {
            String gatewayUrl = paymentService.createPaymentSession("buyer@mail.com", 55L);

            assertThat(gatewayUrl).isEqualTo("https://gateway.url/session/ok");
            assertThat(mocked.constructed()).hasSize(1);
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Long>> pendingPayments
                = (Map<String, Map<String, Long>>) ReflectionTestUtils.getField(paymentService, "pendingPayments");

        assertThat(pendingPayments).isNotNull();
        assertThat(pendingPayments).hasSize(1);
        Map<String, Long> paymentInfo = pendingPayments.values().iterator().next();
        assertThat(paymentInfo.get("buyerId")).isEqualTo(10L);
        assertThat(paymentInfo.get("noteId")).isEqualTo(55L);
    }

    @Test
    void payment_fail() {
        ReflectionTestUtils.setField(paymentService, "storeId", "store");
        ReflectionTestUtils.setField(paymentService, "storePassword", "password");
        ReflectionTestUtils.setField(paymentService, "sslUrl", "https://example.com");

        User user = TestDataBuilder.user(11L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        Note note = TestDataBuilder.note(56L, "Gateway Fail Note", null);

        when(userRepository.findByEmail("buyer@mail.com")).thenReturn(Optional.of(user));
        when(noteRepository.findById(56L)).thenReturn(Optional.of(note));

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            ResponseEntity<Map<String, Object>> response = ResponseEntity.ok(Map.of("status", "FAILED"));

            when(mock.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                    .thenReturn((ResponseEntity) response);
        })) {
            assertThatThrownBy(() -> paymentService.createPaymentSession("buyer@mail.com", 56L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Payment initialization failed");

            assertThat(mocked.constructed()).hasSize(1);
        }
    }

    @Test
    void invalid_transaction() {
        assertThat(paymentService.getBuyerId("INVALID-TX-404")).isNull();
        assertThat(paymentService.getNoteId("INVALID-TX-404")).isNull();
    }
}
