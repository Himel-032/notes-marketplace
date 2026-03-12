package com.notes.notesmarketplace.service;

public interface PaymentService {

    String createPaymentSession(String email, Long noteId);

    void storePendingPayment(String transactionId, Long buyerId, Long noteId);

    Long getBuyerId(String transactionId);

    Long getNoteId(String transactionId);

    void removePendingPayment(String transactionId);

}
