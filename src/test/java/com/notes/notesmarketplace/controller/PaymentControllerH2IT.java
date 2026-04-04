package com.notes.notesmarketplace.controller;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.OrderItemRepository;
import com.notes.notesmarketplace.repository.OrderRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.OrderService;
import com.notes.notesmarketplace.service.PaymentService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentControllerH2IT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Test
    void paymentSuccess_shouldCreateOrderAndOrderItem_inH2() throws Exception {
        User buyer = createUser("buyer-success");
        Note note = createNote(buyer, "Physics", 100.0);

        String trxId = "TRX-SUCCESS-" + UUID.randomUUID();
        paymentService.storePendingPayment(trxId, buyer.getId(), note.getId());

        mockMvc.perform(post("/api/payment/success")
                .param("tran_id", trxId)
                .param("status", "VALID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/success?tran_id=" + trxId));

        boolean purchased = orderService.hasPurchased(buyer.getId(), note.getId());
        assertThat(purchased).isTrue();

        List<Order> buyerOrders = orderRepository.findByBuyerId(buyer.getId());
        assertThat(buyerOrders).hasSize(1);

        Order order = buyerOrders.get(0);
        assertThat(order.getTransactionId()).isEqualTo(trxId);
        assertThat(order.getStatus()).isEqualTo("PAID");
        assertThat(order.getTotalPrice()).isEqualTo(100.0);
        assertThat(order.getOrderNumber()).startsWith("ORD-");

        assertThat(orderItemRepository.existsByOrderBuyerIdAndNoteId(buyer.getId(), note.getId())).isTrue();
        assertThat(paymentService.getBuyerId(trxId)).isNull();
        assertThat(paymentService.getNoteId(trxId)).isNull();
    }

    @Test
    void paymentSuccess_withInvalidStatus_shouldNotCreateOrder() throws Exception {
        User buyer = createUser("buyer-invalid-status");
        Note note = createNote(buyer, "Chemistry", 120.0);

        String trxId = "TRX-STATUS-" + UUID.randomUUID();
        paymentService.storePendingPayment(trxId, buyer.getId(), note.getId());

        mockMvc.perform(post("/api/payment/success")
                .param("tran_id", trxId)
                .param("status", "FAILED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/failed?error=validation"));

        assertThat(orderRepository.findByBuyerId(buyer.getId())).isEmpty();
        assertThat(orderItemRepository.existsByOrderBuyerIdAndNoteId(buyer.getId(), note.getId())).isFalse();

        // Current controller logic exits early for invalid status and keeps pending entry.
        assertThat(paymentService.getBuyerId(trxId)).isEqualTo(buyer.getId());
        assertThat(paymentService.getNoteId(trxId)).isEqualTo(note.getId());

        paymentService.removePendingPayment(trxId);
    }

    @Test
    void paymentSuccess_withUnknownTransaction_shouldRedirectAndNotPersistOrder() throws Exception {
        String trxId = "TRX-UNKNOWN-" + UUID.randomUUID();

        mockMvc.perform(post("/api/payment/success")
                .param("tran_id", trxId)
                .param("status", "VALID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/failed?error=invalid_transaction"));

        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
    }

    @Test
    void paymentFail_shouldCleanupPendingPayment_inH2() throws Exception {
        User buyer = createUser("buyer-fail");
        Note note = createNote(buyer, "Biology", 95.0);

        String trxId = "TRX-FAIL-" + UUID.randomUUID();
        paymentService.storePendingPayment(trxId, buyer.getId(), note.getId());

        mockMvc.perform(post("/api/payment/fail")
                .param("tran_id", trxId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/failed?tran_id=" + trxId));

        assertThat(paymentService.getBuyerId(trxId)).isNull();
        assertThat(paymentService.getNoteId(trxId)).isNull();
        assertThat(orderRepository.findByBuyerId(buyer.getId())).isEmpty();
    }

    @Test
    void paymentCancel_shouldCleanupPendingPayment_inH2() throws Exception {
        User buyer = createUser("buyer-cancel");
        Note note = createNote(buyer, "Mathematics", 150.0);

        String trxId = "TRX-CANCEL-" + UUID.randomUUID();
        paymentService.storePendingPayment(trxId, buyer.getId(), note.getId());

        mockMvc.perform(post("/api/payment/cancel")
                .param("tran_id", trxId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/cancelled?tran_id=" + trxId));

        assertThat(paymentService.getBuyerId(trxId)).isNull();
        assertThat(paymentService.getNoteId(trxId)).isNull();
        assertThat(orderRepository.findByBuyerId(buyer.getId())).isEmpty();
    }

    private User createUser(String emailPrefix) {
        User user = new User();
        user.setName("Buyer User");
        user.setEmail(emailPrefix + "@mail.com");
        user.setPassword("password123");
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Note createNote(User seller, String title, double price) {
        Note note = new Note();
        note.setTitle(title);
        note.setDescription("Integration test note");
        note.setCategory("Science");
        note.setPrice(price);
        note.setPdfUrl("http://example.com/" + title.toLowerCase() + ".pdf");
        note.setSeller(seller);
        return noteRepository.save(note);
    }
}
