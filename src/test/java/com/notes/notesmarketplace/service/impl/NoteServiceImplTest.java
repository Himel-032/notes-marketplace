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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
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

    @Test
    void createNote_invalidFileType() {
        NoteUploadRequest request = TestDataBuilder.noteUploadRequestInvalidType();

        assertThatThrownBy(() -> noteService.uploadNote(request, "seller@mail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only PDF files allowed");

        verify(cloudinaryService, never()).uploadPdf(any());
        verify(cloudinaryService, never()).uploadImage(any());
    }

    @Test
    void updateNote_notOwner() {
        User requester = TestDataBuilder.user(1L, "seller@mail.com", "Seller", true, Set.of(new Role(1L, "SELLER")));
        User owner = TestDataBuilder.user(2L, "owner@mail.com", "Owner", true, Set.of(new Role(1L, "SELLER")));
        Note note = TestDataBuilder.note(11L, "Chem", owner);
        NoteUpdateRequest request = TestDataBuilder.noteUpdateRequest();

        when(userRepository.findByEmail("seller@mail.com")).thenReturn(Optional.of(requester));
        when(noteRepository.findById(11L)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.updateNote(11L, request, "seller@mail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unauthorized");

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void deleteNote_notFound() {
        User seller = TestDataBuilder.user(1L, "seller@mail.com", "Seller", true, Set.of(new Role(1L, "SELLER")));

        when(userRepository.findByEmail("seller@mail.com")).thenReturn(Optional.of(seller));
        when(noteRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.deleteNote(404L, "seller@mail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Note not found");

        verify(cloudinaryService, never()).deletePdf(any());
    }

    @Test
    void searchNotes() {
        User seller = TestDataBuilder.user(1L, "seller@mail.com", "Seller", true, Set.of(new Role(1L, "SELLER")));
        Note titleMatch = TestDataBuilder.note(9L, "Java Fundamentals", seller);
        Note descMatch = TestDataBuilder.note(10L, "Design Patterns", seller);
        descMatch.setDescription("Best java patterns collection");

        when(noteRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("java", "java"))
                .thenReturn(List.of(titleMatch, descMatch));

        List<NoteDto> notes = noteService.searchNotes("java");

        assertThat(notes).hasSize(2);
        assertThat(notes).extracting(NoteDto::getTitle)
                .containsExactlyInAnyOrder("Java Fundamentals", "Design Patterns");
    }
}
