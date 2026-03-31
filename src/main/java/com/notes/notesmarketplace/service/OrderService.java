package com.notes.notesmarketplace.service;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Order;

import java.util.List;

public interface OrderService {

    Order createOrder(Long buyerId, Long noteId, String transactionId);

    boolean hasPurchased(Long buyerId, Long noteId);

    List<Order> getBuyerOrders(Long buyerId);

    List<Note> getBuyerPurchasedNotes(Long buyerId);

}
