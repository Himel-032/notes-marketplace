package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.dto.NoteDto;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.NoteService;
import com.notes.notesmarketplace.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buyer")
@RequiredArgsConstructor
@Slf4j
public class BuyerController {

    private final NoteService noteService;
    private final OrderService orderService;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @GetMapping("/notes")
    public List<NoteDto> browseNotes() {
        return noteService.browseNotes();
    }

    @GetMapping("/notes/search")
    public List<NoteDto> searchNotes(@RequestParam String keyword) {
        return noteService.searchNotes(keyword);
    }

    @GetMapping("/notes/filter")
    public List<NoteDto> filterNotes(@RequestParam String category) {
        return noteService.filterNotes(category);
    }

    @GetMapping("/notes/{id}")
    public NoteDto getNote(@PathVariable Long id) {
        return noteService.getNote(id);
    }

    @GetMapping("/notes/{id}/preview")
    public ResponseEntity<?> getPreviewImage(@PathVariable Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        return ResponseEntity.ok()
                .body(Map.of(
                        "previewImageUrl", note.getPreviewImageUrl(),
                        "title", note.getTitle(),
                        "category", note.getCategory(),
                        "description", note.getDescription(),
                        "price", note.getPrice()
                ));
    }

    @GetMapping("/notes/{id}/full-preview")
    public ResponseEntity<byte[]> getFullPreview(
            @PathVariable Long id,
            Authentication authentication
    ) throws IOException {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean purchased = orderService.hasPurchased(user.getId(), id);

        if (!purchased) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Fetch PDF from Cloudinary
        byte[] pdfBytes = URI.create(note.getPdfUrl()).toURL().openStream().readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(note.getTitle() + ".pdf")
                        .build()
        );
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/notes/{id}/download")
    public ResponseEntity<byte[]> downloadNote(
            @PathVariable Long id,
            Authentication authentication
    ) throws IOException {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean purchased = orderService.hasPurchased(user.getId(), id);

        if (!purchased) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Fetch PDF from Cloudinary
        byte[] pdfBytes = URI.create(note.getPdfUrl()).toURL().openStream().readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(note.getTitle() + ".pdf")
                        .build()
        );
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        log.info("Fetching orders for user: {}", authentication.getName());
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("User found with ID: {}", user.getId());
        List<Order> orders = orderService.getBuyerOrders(user.getId());
        log.info("Found {} orders for buyer ID: {}", orders.size(), user.getId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my-downloads")
    public ResponseEntity<?> getMyDownloads(Authentication authentication) {
        log.info("Fetching downloads for user: {}", authentication.getName());
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("User found with ID: {}", user.getId());
        List<Note> notes = orderService.getBuyerPurchasedNotes(user.getId());
        log.info("Found {} purchased notes for buyer ID: {}", notes.size(), user.getId());
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/purchased/{noteId}")
    public ResponseEntity<?> checkPurchased(
            @PathVariable Long noteId,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean purchased = orderService.hasPurchased(user.getId(), noteId);
        return ResponseEntity.ok(Map.of("purchased", purchased));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getBuyerStats(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Note> purchasedNotes = orderService.getBuyerPurchasedNotes(user.getId());
        List<Order> orders = orderService.getBuyerOrders(user.getId());

        double totalSpent = orders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();

        long notesAvailable = noteRepository.count();

        return ResponseEntity.ok(Map.of(
                "totalPurchases", purchasedNotes.size(),
                "totalSpent", totalSpent,
                "notesAvailable", notesAvailable
        ));
    }
}
