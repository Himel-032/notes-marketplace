package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.OrderService;
import com.notes.notesmarketplace.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Test
    void paymentSuccess_h2Integration() throws Exception {

        //  save data in H2
        User buyer = new User();
        buyer.setName("Buyer User");
        buyer.setEmail("buyer@mail.com");
        buyer.setPassword("password123");
        buyer.setEnabled(true);
        buyer = userRepository.save(buyer);

        Note note = new Note();
        note.setTitle("Physics");
        note.setPrice(100.0);
        note.setPdfUrl("http://example.com/physics.pdf");
        note.setSeller(buyer);
        note = noteRepository.save(note);

        String trxId = "TRX-1";
        paymentService.storePendingPayment(trxId, buyer.getId(), note.getId());

        // call API
        mockMvc.perform(post("/api/payment/success")
                .param("tran_id", trxId)
                .param("status", "VALID"))
                .andExpect(status().is3xxRedirection());

        //  verify DB (H2)
        boolean purchased = orderService.hasPurchased(buyer.getId(), note.getId());
        assertThat(purchased).isTrue();
    }
}
