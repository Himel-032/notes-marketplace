package com.notes.notesmarketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.User;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findBySeller(User seller);

    List<Note> findByTitleContainingIgnoreCase(String title);

    List<Note> findByDescriptionContainingIgnoreCase(String description);

    List<Note> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title,
            String description
    );

    List<Note> findByCategoryIgnoreCase(String category);

    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.note.id = :noteId")
    Long countSalesByNoteId(@Param("noteId") Long noteId);

}
