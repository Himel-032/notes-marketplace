package com.notes.notesmarketplace.service;

public interface PaymentService {

    default String createPaymentSession(String email, Long noteId) {
        return createPaymentSession(email, noteId, "CARD");
    }

    String createPaymentSession(String email, Long noteId, String paymentMethod);

    default void storePendingPayment(String transactionId, Long buyerId, Long noteId) {
        storePendingPayment(transactionId, buyerId, noteId, "CARD");
    }

    void storePendingPayment(String transactionId, Long buyerId, Long noteId, String paymentMethod);

    Long getBuyerId(String transactionId);

    Long getNoteId(String transactionId);

    String getPaymentMethod(String transactionId);

    void removePendingPayment(String transactionId);

}
