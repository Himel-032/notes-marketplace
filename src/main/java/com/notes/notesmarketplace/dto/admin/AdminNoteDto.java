package com.notes.notesmarketplace.dto.admin;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminNoteDto {
    Long id;
    String title;
    String category;
    Double price;
    Long sellerId;
    String sellerEmail;
    String pdfUrl;
}
