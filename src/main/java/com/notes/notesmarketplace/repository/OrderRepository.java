package com.notes.notesmarketplace.repository;

import com.notes.notesmarketplace.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerId(Long buyerId);

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Order> findByBuyerIdOrderByCreatedAtAsc(Long buyerId);

    @Query("select coalesce(sum(o.totalPrice), 0) from Order o where o.status = 'PAID'")
    double getTotalSales();

}
