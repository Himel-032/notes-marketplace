package com.notes.notesmarketplace.service.impl;

import com.notes.notesmarketplace.dto.NoteUpdateRequest;
import com.notes.notesmarketplace.dto.NoteUploadRequest;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.CloudinaryService;
import com.notes.notesmarketplace.service.NoteService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public Note uploadNote(NoteUploadRequest request, String sellerEmail) {

        if (!request.getFile().getContentType().equals("application/pdf")) {
            throw new RuntimeException("Only PDF files allowed");
        }

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        String pdfUrl = cloudinaryService.uploadPdf(request.getFile());

        Note note = Note.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .pdfUrl(pdfUrl)
                .seller(seller)
                .build();

        return noteRepository.save(note);
    }

    @Override
    public List<Note> getSellerNotes(String sellerEmail) {

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        return noteRepository.findBySeller(seller);
    }

    @Override
    public Note updateNote(Long noteId, NoteUpdateRequest request, String sellerEmail) {

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Update note fields
        note.setTitle(request.getTitle());
        note.setDescription(request.getDescription());
        note.setCategory(request.getCategory());
        note.setPrice(request.getPrice());

        return noteRepository.save(note);
    }

    @Override
    public void deleteNote(Long noteId, String sellerEmail) {

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Delete PDF from Cloudinary first
        cloudinaryService.deletePdf(note.getPdfUrl());

        // Then delete from database
        noteRepository.delete(note);
    }
}
