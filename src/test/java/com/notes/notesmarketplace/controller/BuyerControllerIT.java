package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.dto.NoteDto;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.NoteService;
import com.notes.notesmarketplace.service.OrderService;
import com.notes.notesmarketplace.support.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BuyerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private NoteRepository noteRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void browseNotes_shouldReturnPagedData() throws Exception {
        when(noteService.browseNotes()).thenReturn(List.of(
                NoteDto.builder().id(1L).title("Physics").description("d").category("Science").price(100.0).build()
        ));

        mockMvc.perform(get("/api/buyer/notes").with(user("buyer@mail.com").roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Physics"));
    }

    @Test
    void checkPurchased_shouldReturnTrueFlag() throws Exception {
        User user = TestDataBuilder.user(5L, "buyer@mail.com", "Buyer", true, null);
        when(userRepository.findByEmail("buyer@mail.com")).thenReturn(Optional.of(user));
        when(orderService.hasPurchased(5L, 9L)).thenReturn(true);

        mockMvc.perform(get("/api/buyer/purchased/9").with(user("buyer@mail.com").roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchased").value(true));
    }
}
