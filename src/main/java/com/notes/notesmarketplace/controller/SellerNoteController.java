package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.dto.NoteUpdateRequest;
import com.notes.notesmarketplace.dto.NoteUploadRequest;
import com.notes.notesmarketplace.dto.NoteDto;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.service.NoteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;

@RestController
@RequestMapping("/seller/notes")
@RequiredArgsConstructor
public class SellerNoteController {

    private final NoteService noteService;

    @PostMapping("/upload")
    public Note uploadNote(
            @ModelAttribute NoteUploadRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return noteService.uploadNote(request, email);
    }

    @GetMapping("/my-notes")
    public List<NoteDto> getMyNotes(Authentication authentication) {

        String email = authentication.getName();

        return noteService.getSellerNoteDtos(email);
    }

    @PutMapping("/{id}")
    public Note updateNote(
            @PathVariable Long id,
            @RequestBody NoteUpdateRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return noteService.updateNote(id, request, email);
    }

    @DeleteMapping("/{id}")
    public String deleteNote(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        noteService.deleteNote(id, email);

        return "Note deleted successfully";
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewPdf(
            @PathVariable Long id,
            Authentication authentication
    ) throws IOException {

        String email = authentication.getName();
        List<Note> sellerNotes = noteService.getSellerNotes(email);

        Note note = sellerNotes.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Note not found or unauthorized"));

        // Fetch PDF from Cloudinary
        URI pdfUri = URI.create(note.getPdfUrl());
        URLConnection connection = pdfUri.toURL().openConnection();

        byte[] pdfBytes;
        try (InputStream inputStream = connection.getInputStream()) {
            pdfBytes = inputStream.readAllBytes();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("Content-Disposition", "inline; filename=\"" + note.getTitle() + ".pdf\"");
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id,
            Authentication authentication
    ) throws IOException {

        String email = authentication.getName();
        List<Note> sellerNotes = noteService.getSellerNotes(email);

        Note note = sellerNotes.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Note not found or unauthorized"));

        // Fetch PDF from Cloudinary
        URI pdfUri = URI.create(note.getPdfUrl());
        URLConnection connection = pdfUri.toURL().openConnection();

        byte[] pdfBytes;
        try (InputStream inputStream = connection.getInputStream()) {
            pdfBytes = inputStream.readAllBytes();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", note.getTitle() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
