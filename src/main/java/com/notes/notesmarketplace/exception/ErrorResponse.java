package com.notes.notesmarketplace.exception;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class ErrorResponse {
    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
    List<String> details;
}
