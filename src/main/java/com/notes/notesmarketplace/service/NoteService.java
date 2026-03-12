package com.notes.notesmarketplace.service;


import com.notes.notesmarketplace.dto.NoteUpdateRequest;
import com.notes.notesmarketplace.dto.NoteUploadRequest;
import com.notes.notesmarketplace.dto.NoteDto;
import com.notes.notesmarketplace.model.Note;

import java.util.List;

public interface NoteService {

    Note uploadNote(NoteUploadRequest request, String sellerEmail);

    List<Note> getSellerNotes(String sellerEmail);

    Note updateNote(Long noteId, NoteUpdateRequest request, String sellerEmail);

    void deleteNote(Long noteId, String sellerEmail);

    List<NoteDto> browseNotes();

    List<NoteDto> searchNotes(String keyword);

    List<NoteDto> filterNotes(String category);

    NoteDto getNote(Long id);
}
