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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    void createOrder_shouldPersistOrderAndItem() {
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
    void hasPurchased_shouldReturnTrue_whenOrderItemExists() {
        when(orderItemRepository.existsByOrderBuyerIdAndNoteId(1L, 2L)).thenReturn(true);

        boolean purchased = orderService.hasPurchased(1L, 2L);

        assertThat(purchased).isTrue();
    }

    @Test
    void getBuyerOrders_shouldDelegateToRepository() {
        User buyer = TestDataBuilder.user(7L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        when(orderRepository.findByBuyerIdOrderByCreatedAtDesc(7L))
                .thenReturn(List.of(TestDataBuilder.order(1L, buyer, 120.0, "TRX-2")));

        List<Order> orders = orderService.getBuyerOrders(7L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getTransactionId()).isEqualTo("TRX-2");
    }

    @Test
    void getBuyerPurchasedNotes_shouldMapOrderItemsToNotes() {
        User buyer = TestDataBuilder.user(4L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        Note note = TestDataBuilder.note(9L, "Algebra", buyer);
        Order order = TestDataBuilder.order(3L, buyer, 100.0, "TRX-3");
        OrderItem item = OrderItem.builder().id(1L).order(order).note(note).price(100.0).build();

        when(orderItemRepository.findByOrderBuyerId(4L)).thenReturn(List.of(item));

        List<Note> notes = orderService.getBuyerPurchasedNotes(4L);

        assertThat(notes).hasSize(1);
        assertThat(notes.get(0).getTitle()).isEqualTo("Algebra");
    }
}
