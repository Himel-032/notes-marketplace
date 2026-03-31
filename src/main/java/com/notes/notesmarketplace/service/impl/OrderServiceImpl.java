package com.notes.notesmarketplace.service.impl;

import com.notes.notesmarketplace.factory.OrderFactory;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.OrderItem;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.OrderItemRepository;
import com.notes.notesmarketplace.repository.OrderRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.OrderService;
import com.notes.notesmarketplace.service.payment.PaymentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final OrderFactory orderFactory;
    private final PaymentContext paymentContext;

    @Override
    public Order createOrder(Long buyerId, Long noteId, String transactionId, String paymentMethod) {

        User buyer = userRepository.findById(buyerId).orElseThrow();
        Note note = noteRepository.findById(noteId).orElseThrow();

        paymentContext.pay(paymentMethod, note.getPrice());

        Order order = orderFactory.createOrder(buyer, transactionId, note.getPrice());

        orderRepository.save(order);

        OrderItem item = OrderItem.builder()
                .order(order)
                .note(note)
                .price(note.getPrice())
                .build();

        orderItemRepository.save(item);

        return order;
    }

    @Override
    public boolean hasPurchased(Long buyerId, Long noteId) {

        return orderItemRepository
                .existsByOrderBuyerIdAndNoteId(buyerId, noteId);
    }

    @Override
    public List<Order> getBuyerOrders(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    @Override
    public List<Note> getBuyerPurchasedNotes(Long buyerId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderBuyerId(buyerId);
        return orderItems.stream()
                .map(OrderItem::getNote)
                .collect(Collectors.toList());
    }
}
