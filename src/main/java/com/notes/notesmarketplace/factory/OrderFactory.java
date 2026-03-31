package com.notes.notesmarketplace.factory;

import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderFactory {

    public Order createOrder(User buyer, String transactionId, Double totalPrice) {
        return Order.builder()
                .buyer(buyer)
                .transactionId(transactionId)
                .totalPrice(totalPrice)
                .status("PAID")
                .createdAt(LocalDateTime.now())
                .build();
    }
}