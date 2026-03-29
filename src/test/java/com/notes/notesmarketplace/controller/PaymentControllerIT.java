package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.service.OrderService;
import com.notes.notesmarketplace.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createPayment_shouldReturnGatewayUrl() throws Exception {
        when(paymentService.createPaymentSession("buyer@mail.com", 5L)).thenReturn("https://gateway.url");

        mockMvc.perform(post("/api/payment/pay")
                .with(user("buyer@mail.com").roles("BUYER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"noteId\":5}"))
                .andExpect(status().isOk())
                .andExpect(content().string("https://gateway.url"));
    }

    @Test
    void paymentSuccess_shouldCreateOrderAndRedirect() throws Exception {
        when(paymentService.getBuyerId("TRX-99")).thenReturn(10L);
        when(paymentService.getNoteId("TRX-99")).thenReturn(50L);
        doNothing().when(paymentService).removePendingPayment("TRX-99");

        mockMvc.perform(post("/api/payment/success")
                .param("tran_id", "TRX-99")
                .param("status", "VALID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/success?tran_id=TRX-99"));

        verify(orderService).createOrder(10L, 50L, "TRX-99");
        verify(paymentService).removePendingPayment("TRX-99");
    }

    @Test
    void failedPaymentCallback_shouldRedirectToFailedPage() throws Exception {
        doNothing().when(paymentService).removePendingPayment("TRX-FAIL");

        mockMvc.perform(post("/api/payment/fail")
                .param("tran_id", "TRX-FAIL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/failed?tran_id=TRX-FAIL"));

        verify(paymentService).removePendingPayment("TRX-FAIL");
    }

    @Test
    void invalidTransactionId_shouldRedirectToInvalidTransactionError() throws Exception {
        when(paymentService.getBuyerId("TRX-INVALID")).thenReturn(null);
        when(paymentService.getNoteId("TRX-INVALID")).thenReturn(null);

        mockMvc.perform(post("/api/payment/success")
                .param("tran_id", "TRX-INVALID")
                .param("status", "VALID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/failed?error=invalid_transaction"));
    }
}
