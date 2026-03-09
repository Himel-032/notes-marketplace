package com.notes.notesmarketplace.dto;



import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class NoteUploadRequest {

    private String title;

    private String description;

    private String category;

    private Double price;

    private MultipartFile file;
}
