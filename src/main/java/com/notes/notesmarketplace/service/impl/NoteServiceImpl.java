package com.notes.notesmarketplace.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.notes.notesmarketplace.dto.NoteDto;
import com.notes.notesmarketplace.dto.NoteUpdateRequest;
import com.notes.notesmarketplace.dto.NoteUploadRequest;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.CloudinaryService;
import com.notes.notesmarketplace.service.NoteService;
import com.notes.notesmarketplace.service.PdfPreviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PdfPreviewService pdfPreviewService;

        // Local registry works as a lightweight factory for browsing strategies.
        private final Map<BrowseMode, NoteBrowseStrategy> browseStrategies = Map.of(
            BrowseMode.ALL, new BrowseAllNotesStrategy(),
            BrowseMode.SEARCH, new SearchNotesStrategy(),
            BrowseMode.FILTER, new FilterNotesStrategy()
        );

    @Override
    public Note uploadNote(NoteUploadRequest request, String sellerEmail) {

        if (!request.getFile().getContentType().equals("application/pdf")) {
            throw new RuntimeException("Only PDF files allowed");
        }

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        //  Generate preview image from PDF
        byte[] previewBytes = pdfPreviewService.generateFirstPagePreview(request.getFile());

        //  Upload preview image to Cloudinary
        String previewUrl = cloudinaryService.uploadImage(previewBytes);

        String pdfUrl = cloudinaryService.uploadPdf(request.getFile());

        Note note = Note.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .pdfUrl(pdfUrl)
                .previewImageUrl(previewUrl)
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

    @Override
    public List<NoteDto> browseNotes() {
        return browseStrategyFactory(BrowseMode.ALL).getNotes("", "");
    }

    @Override
    public List<NoteDto> searchNotes(String keyword) {
        return browseStrategyFactory(BrowseMode.SEARCH).getNotes(keyword, "");
    }

    @Override
    public List<NoteDto> filterNotes(String category) {
        return browseStrategyFactory(BrowseMode.FILTER).getNotes("", category);
    }

    @Override
    public NoteDto getNote(Long id) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        return mapToDTO(note);
    }

    @Override
    public Long getSalesCount(Long noteId) {
        Long count = noteRepository.countSalesByNoteId(noteId);
        return count != null ? count : 0;
    }

    @Override
    public List<NoteDto> getSellerNoteDtos(String sellerEmail) {
        return getSellerNotes(sellerEmail)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private NoteDto mapToDTO(Note note) {

        return NoteDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .description(note.getDescription())
                .category(note.getCategory())
                .price(note.getPrice())
                .previewImageUrl(note.getPreviewImageUrl())
                .salesCount(getSalesCount(note.getId()))
                .build();
    }

    private NoteBrowseStrategy browseStrategyFactory(BrowseMode browseMode) {
        NoteBrowseStrategy strategy = browseStrategies.get(browseMode);
        if (strategy == null) {
            throw new RuntimeException("Unsupported browse mode");
        }
        return strategy;
    }

    private enum BrowseMode {
        ALL,
        SEARCH,
        FILTER
    }

    private interface NoteBrowseStrategy {
        List<NoteDto> getNotes(String keyword, String category);
    }

    private class BrowseAllNotesStrategy implements NoteBrowseStrategy {
        @Override
        public List<NoteDto> getNotes(String keyword, String category) {
            return toDtoList(noteRepository::findAll);
        }
    }

    private class SearchNotesStrategy implements NoteBrowseStrategy {
        @Override
        public List<NoteDto> getNotes(String keyword, String category) {
            return toDtoList(() -> noteRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword));
        }
    }

    private class FilterNotesStrategy implements NoteBrowseStrategy {
        @Override
        public List<NoteDto> getNotes(String keyword, String category) {
            return toDtoList(() -> noteRepository.findByCategoryIgnoreCase(category));
        }
    }

    private List<NoteDto> toDtoList(Supplier<List<Note>> notesSupplier) {
        return notesSupplier.get()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

}
