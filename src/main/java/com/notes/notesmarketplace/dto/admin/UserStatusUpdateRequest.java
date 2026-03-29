package com.notes.notesmarketplace.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStatusUpdateRequest {

    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
