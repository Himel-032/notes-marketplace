package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.dto.NoteUpdateRequest;
import com.notes.notesmarketplace.service.NoteService;
import com.notes.notesmarketplace.support.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    @WithMockUser(username = "seller@mail.com", roles = {"SELLER"})
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
    @WithMockUser(username = "seller@mail.com", roles = {"SELLER"})
    void getMyNotes_shouldReturnSellerNotes() throws Exception {
        when(noteService.getSellerNotes("seller@mail.com"))
                .thenReturn(List.of(TestDataBuilder.note(5L, "Core Spring", null)));

        mockMvc.perform(get("/seller/notes/my-notes").with(user("seller@mail.com").roles("SELLER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L))
                .andExpect(jsonPath("$[0].title").value("Core Spring"));
    }

    @Test
    @WithMockUser(username = "seller@mail.com", roles = {"SELLER"})
    void updateNote_shouldReturnUpdatedNote() throws Exception {
        Note updated = TestDataBuilder.note(11L, "Updated Title", null);
        when(noteService.updateNote(eq(11L), any(NoteUpdateRequest.class), eq("seller@mail.com")))
                .thenReturn(updated);

        mockMvc.perform(put("/seller/notes/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"title\":\"Updated Title\"," +
                                "\"description\":\"Updated Desc\"," +
                                "\"category\":\"Math\"," +
                                "\"price\":120.0}")
                        .with(user("seller@mail.com").roles("SELLER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(username = "seller@mail.com", roles = {"SELLER"})
    void deleteNote_shouldReturnSuccessMessage() throws Exception {
        doNothing().when(noteService).deleteNote(11L, "seller@mail.com");

        mockMvc.perform(delete("/seller/notes/11").with(user("seller@mail.com").roles("SELLER")))
                .andExpect(status().isOk())
                .andExpect(content().string("Note deleted successfully"));
    }
}
