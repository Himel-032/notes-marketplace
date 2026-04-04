package com.notes.notesmarketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor  // Lombok annotation to generate constructor, getters, setters, etc.
public class AuthResponse {
    private String message;
}
