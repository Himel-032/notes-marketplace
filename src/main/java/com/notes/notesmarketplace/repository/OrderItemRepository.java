package com.notes.notesmarketplace.repository;

import com.notes.notesmarketplace.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    interface TopSellingNoteProjection {
        String getTitle();
        Long getSoldCount();
    }

    boolean existsByOrderBuyerIdAndNoteId(Long buyerId, Long noteId);

    boolean existsByNoteId(Long noteId);

    List<OrderItem> findByOrderBuyerId(Long buyerId);

    @Query("""
            select oi.note.title as title, count(oi.id) as soldCount
            from OrderItem oi
            join oi.order o
            where upper(o.status) = 'PAID'
            group by oi.note.title
            order by count(oi.id) desc
            """)
    List<TopSellingNoteProjection> findTopSellingPaidNotes(Pageable pageable);

}
