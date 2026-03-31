package com.notes.notesmarketplace.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private Long noteId;
    private String paymentMethod;
}
