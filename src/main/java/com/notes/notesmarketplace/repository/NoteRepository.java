package com.notes.notesmarketplace.repository;




import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findBySeller(User seller);

}
