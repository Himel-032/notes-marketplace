package com.notes.notesmarketplace.repository;

import com.notes.notesmarketplace.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    boolean existsByOrderBuyerIdAndNoteId(Long buyerId, Long noteId);

    boolean existsByNoteId(Long noteId);

    List<OrderItem> findByOrderBuyerId(Long buyerId);

}
