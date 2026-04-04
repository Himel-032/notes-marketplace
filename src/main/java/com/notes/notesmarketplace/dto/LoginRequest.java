package com.notes.notesmarketplace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;  // Lombok annotation to generate getters, setters, toString, equals, and hashCode methods

@Data
public class LoginRequest {
    @Email
    private String email;

    @NotBlank
    private String password;
}
