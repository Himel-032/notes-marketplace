package com.notes.notesmarketplace.service.impl;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.OrderItem;
import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.OrderItemRepository;
import com.notes.notesmarketplace.repository.OrderRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.support.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_success() {
        User buyer = TestDataBuilder.user(10L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        Note note = TestDataBuilder.note(20L, "History", buyer);

        when(userRepository.findById(10L)).thenReturn(Optional.of(buyer));
        when(noteRepository.findById(20L)).thenReturn(Optional.of(note));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));

        Order created = orderService.createOrder(10L, 20L, "TRX-1");

        assertThat(created.getTransactionId()).isEqualTo("TRX-1");
        assertThat(created.getTotalPrice()).isEqualTo(100.0);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void getOrderById() {
        User buyer = TestDataBuilder.user(7L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        when(orderRepository.findByBuyerIdOrderByCreatedAtDesc(7L))
                .thenReturn(List.of(TestDataBuilder.order(1L, buyer, 120.0, "TRX-2")));

        List<Order> orders = orderService.getBuyerOrders(7L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void calculateTotalPrice() {
        User buyer = TestDataBuilder.user(10L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        Note note = TestDataBuilder.note(20L, "History", buyer);
        note.setPrice(250.0);

        when(userRepository.findById(10L)).thenReturn(Optional.of(buyer));
        when(noteRepository.findById(20L)).thenReturn(Optional.of(note));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));

        Order order = orderService.createOrder(10L, 20L, "TRX-250");

        assertThat(order.getTotalPrice()).isEqualTo(250.0);
    }

    @Test
    void createOrder_emptyCart() {
        User buyer = TestDataBuilder.user(10L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));

        when(userRepository.findById(10L)).thenReturn(Optional.of(buyer));
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(10L, 999L, "TRX-EMPTY"))
                .isInstanceOf(NoSuchElementException.class);

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void getOrdersByUser_notFound() {
        when(orderRepository.findByBuyerIdOrderByCreatedAtDesc(9999L)).thenReturn(List.of());

        List<Order> orders = orderService.getBuyerOrders(9999L);

        assertThat(orders).isEmpty();
    }

    @Test
    void hasPurchased_shouldReturnTrueWhenOrderItemExists() {
        when(orderItemRepository.existsByOrderBuyerIdAndNoteId(10L, 20L)).thenReturn(true);

        boolean purchased = orderService.hasPurchased(10L, 20L);

        assertThat(purchased).isTrue();
    }

    @Test
    void getBuyerPurchasedNotes_shouldReturnNotesFromOrderItems() {
        User buyer = TestDataBuilder.user(10L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        Note note = TestDataBuilder.note(20L, "History", buyer);
        Order order = TestDataBuilder.order(1L, buyer, 100.0, "TRX-HISTORY");
        OrderItem orderItem = OrderItem.builder()
                .id(99L)
                .order(order)
                .note(note)
                .price(100.0)
                .build();

        when(orderItemRepository.findByOrderBuyerId(10L)).thenReturn(List.of(orderItem));

        List<Note> purchasedNotes = orderService.getBuyerPurchasedNotes(10L);

        assertThat(purchasedNotes).hasSize(1);
        assertThat(purchasedNotes.get(0).getId()).isEqualTo(20L);
        assertThat(purchasedNotes.get(0).getTitle()).isEqualTo("History");
    }
}
