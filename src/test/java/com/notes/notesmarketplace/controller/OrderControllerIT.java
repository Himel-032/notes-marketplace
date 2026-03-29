package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.OrderService;
import com.notes.notesmarketplace.service.PaymentService;
import com.notes.notesmarketplace.support.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void createOrder_success() throws Exception {
        when(paymentService.getBuyerId("TRX-100")).thenReturn(10L);
        when(paymentService.getNoteId("TRX-100")).thenReturn(20L);
        doNothing().when(paymentService).removePendingPayment("TRX-100");

        mockMvc.perform(post("/api/payment/success")
                        .param("tran_id", "TRX-100")
                        .param("status", "VALID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/success?tran_id=TRX-100"));

        verify(orderService).createOrder(10L, 20L, "TRX-100");
    }

    @Test
    @WithMockUser(username = "buyer@mail.com", roles = {"BUYER"})
    void getOrderById() throws Exception {
        User buyer = TestDataBuilder.user(9L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        Order order = TestDataBuilder.order(77L, buyer, 199.0, "TRX-77");

        when(userRepository.findByEmail("buyer@mail.com")).thenReturn(Optional.of(buyer));
        when(orderService.getBuyerOrders(9L)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/buyer/my-orders").with(user("buyer@mail.com").roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(77L))
                .andExpect(jsonPath("$[0].transactionId").value("TRX-77"));
    }
}
