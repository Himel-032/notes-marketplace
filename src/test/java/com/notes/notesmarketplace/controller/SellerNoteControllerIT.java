package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.service.NoteService;
import com.notes.notesmarketplace.support.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SellerNoteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @Test
    void uploadNote_shouldReturnCreatedNote() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "pdf".getBytes());
        Note note = TestDataBuilder.note(3L, "Uploaded Note", null);

        when(noteService.uploadNote(any(), eq("seller@mail.com"))).thenReturn(note);

        mockMvc.perform(multipart("/seller/notes/upload")
                        .file(file)
                        .param("title", "Uploaded Note")
                        .param("description", "Description")
                        .param("category", "Science")
                        .param("price", "200")
                        .with(user("seller@mail.com").roles("SELLER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.title").value("Uploaded Note"));
    }

    @Test
    void deleteNote_shouldReturnSuccessMessage() throws Exception {
        doNothing().when(noteService).deleteNote(11L, "seller@mail.com");

        mockMvc.perform(delete("/seller/notes/11").with(user("seller@mail.com").roles("SELLER")))
                .andExpect(status().isOk())
                .andExpect(content().string("Note deleted successfully"));
    }
}
