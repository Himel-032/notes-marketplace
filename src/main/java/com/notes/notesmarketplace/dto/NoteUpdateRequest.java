package com.notes.notesmarketplace.dto;

import lombok.Data;

@Data
public class NoteUpdateRequest {

    private String title;

    private String description;

    private String category;

    private Double price;
}
