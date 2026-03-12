package com.notes.notesmarketplace.repository;

import com.notes.notesmarketplace.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerId(Long buyerId);

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

}
