package com.notes.notesmarketplace.dto.admin;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class AdminUserDto {
    Long id;
    String name;
    String email;
    boolean enabled;
    Set<String> roles;
}
