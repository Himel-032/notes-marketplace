package com.notes.notesmarketplace.service.impl;

import com.notes.notesmarketplace.dto.NoteDto;
import com.notes.notesmarketplace.dto.NoteUpdateRequest;
import com.notes.notesmarketplace.dto.NoteUploadRequest;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.CloudinaryService;
import com.notes.notesmarketplace.service.PdfPreviewService;
import com.notes.notesmarketplace.support.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private PdfPreviewService pdfPreviewService;

    @InjectMocks
    private NoteServiceImpl noteService;

    @Test
    void createNote_success() {
        User seller = TestDataBuilder.user(1L, "seller@mail.com", "Seller", true, Set.of(new Role(1L, "SELLER")));
        NoteUploadRequest request = TestDataBuilder.noteUploadRequestPdf();

        when(userRepository.findByEmail("seller@mail.com")).thenReturn(Optional.of(seller));
        when(pdfPreviewService.generateFirstPagePreview(request.getFile())).thenReturn(new byte[]{1, 2, 3});
        when(cloudinaryService.uploadImage(any())).thenReturn("https://img.url");
        when(cloudinaryService.uploadPdf(request.getFile())).thenReturn("https://pdf.url");
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note saved = noteService.uploadNote(request, "seller@mail.com");

        assertThat(saved.getTitle()).isEqualTo("Physics Note");
        assertThat(saved.getPdfUrl()).isEqualTo("https://pdf.url");
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void updateNote_success() {
        User seller = TestDataBuilder.user(1L, "seller@mail.com", "Seller", true, Set.of(new Role(1L, "SELLER")));
        Note note = TestDataBuilder.note(11L, "Chem", seller);
        NoteUpdateRequest request = TestDataBuilder.noteUpdateRequest();

        when(userRepository.findByEmail("seller@mail.com")).thenReturn(Optional.of(seller));
        when(noteRepository.findById(11L)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note updated = noteService.updateNote(11L, request, "seller@mail.com");

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getPrice()).isEqualTo(150.0);
    }

    @Test
    void deleteNote_success() {
        User seller = TestDataBuilder.user(1L, "seller@mail.com", "Seller", true, Set.of(new Role(1L, "SELLER")));
        Note note = TestDataBuilder.note(2L, "Math", seller);

        when(userRepository.findByEmail("seller@mail.com")).thenReturn(Optional.of(seller));
        when(noteRepository.findById(2L)).thenReturn(Optional.of(note));
        doNothing().when(cloudinaryService).deletePdf(note.getPdfUrl());

        noteService.deleteNote(2L, "seller@mail.com");

        verify(cloudinaryService).deletePdf(note.getPdfUrl());
        verify(noteRepository).delete(note);
    }

    @Test
    void getNoteById() {
        User seller = TestDataBuilder.user(1L, "seller@mail.com", "Seller", true, Set.of(new Role(1L, "SELLER")));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(TestDataBuilder.note(1L, "Biology", seller)));

        NoteDto note = noteService.getNote(1L);

        assertThat(note.getId()).isEqualTo(1L);
        assertThat(note.getTitle()).isEqualTo("Biology");
    }
}
