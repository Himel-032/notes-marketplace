package com.notes.notesmarketplace.integration;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.OrderItem;
import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.OrderItemRepository;
import com.notes.notesmarketplace.repository.OrderRepository;
import com.notes.notesmarketplace.repository.RoleRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MarketplaceFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentService paymentService;

    @Test
    void buyerNotesApi_shouldReturnSalesCount() throws Exception {
        User seller = createUser("seller-flow@mail.com", "Seller Flow");
        User buyer = createUser("buyer-flow@mail.com", "Buyer Flow");

        Note note = noteRepository.save(Note.builder()
                .title("Spring Sales Note")
                .description("desc")
                .category("Science")
                .price(120.0)
                .pdfUrl("https://cdn.example.com/spring-sales.pdf")
                .previewImageUrl("https://cdn.example.com/spring-sales.png")
                .seller(seller)
                .build());

        Order order = orderRepository.save(Order.builder()
                .buyer(buyer)
                .transactionId("TRX-SALES-1")
                .totalPrice(120.0)
                .status("PAID")
                .createdAt(LocalDateTime.now())
                .build());

        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .note(note)
                .price(120.0)
                .build());

        mockMvc.perform(get("/api/buyer/notes")
                        .with(user("buyer-flow@mail.com").roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Sales Note"))
                .andExpect(jsonPath("$[0].salesCount").value(1));
    }

    @Test
    void paymentSuccessCallback_shouldCreateOrderAndPersistIt() throws Exception {
        User seller = createUser("seller-pay@mail.com", "Seller Pay");
        User buyer = createUser("buyer-pay@mail.com", "Buyer Pay");

        Note note = noteRepository.save(Note.builder()
                .title("Payment Note")
                .description("desc")
                .category("Science")
                .price(220.0)
                .pdfUrl("https://cdn.example.com/payment-note.pdf")
                .previewImageUrl("https://cdn.example.com/payment-note.png")
                .seller(seller)
                .build());

        paymentService.storePendingPayment("TRX-CB-100", buyer.getId(), note.getId());

        mockMvc.perform(post("/api/payment/success")
                        .param("tran_id", "TRX-CB-100")
                        .param("status", "VALID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/success?tran_id=TRX-CB-100"));

        assertThat(orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId())).hasSize(1);
        assertThat(orderItemRepository.findByOrderBuyerId(buyer.getId())).hasSize(1);
    }

    @Test
    void authApi_shouldRegisterAndLoginUserFlow() throws Exception {
        roleRepository.save(new Role(null, "BUYER"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"Flow User\"," +
                                "\"email\":\"flow-user@mail.com\"," +
                                "\"password\":\"Password123!\"," +
                                "\"role\":\"BUYER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"flow-user@mail.com\"," +
                                "\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded-password");
        return userRepository.save(user);
    }
}
