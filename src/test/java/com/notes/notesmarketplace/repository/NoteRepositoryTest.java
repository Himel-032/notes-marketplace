package com.notes.notesmarketplace.repository;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.OrderItem;
import com.notes.notesmarketplace.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void countSalesByNoteId_shouldReturnOrderItemCountForNote() {
        User seller = new User();
        seller.setName("Seller");
        seller.setEmail("seller@mail.com");
        seller.setPassword("encoded-password");
        seller = userRepository.save(seller);

        User buyer = new User();
        buyer.setName("Buyer");
        buyer.setEmail("buyer@mail.com");
        buyer.setPassword("encoded-password");
        buyer = userRepository.save(buyer);

        Note note = Note.builder()
                .title("Spring Notes")
                .description("desc")
                .category("Science")
                .price(150.0)
                .pdfUrl("https://cdn.example.com/spring.pdf")
                .previewImageUrl("https://cdn.example.com/spring.png")
                .seller(seller)
                .build();
        note = noteRepository.save(note);

        Order order = Order.builder()
                .buyer(buyer)
                .transactionId("TRX-123")
                .totalPrice(150.0)
                .status("PAID")
                .createdAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);

        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .note(note)
                .price(150.0)
                .build());

        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .note(note)
                .price(150.0)
                .build());

        Long salesCount = noteRepository.countSalesByNoteId(note.getId());

        assertThat(salesCount).isEqualTo(2L);
    }

    @Test
    void countSalesByNoteId_shouldReturnZeroWhenNoOrderItemsExist() {
        User seller = new User();
        seller.setName("Seller 2");
        seller.setEmail("seller2@mail.com");
        seller.setPassword("encoded-password");
        seller = userRepository.save(seller);

        Note note = Note.builder()
                .title("Unsold Note")
                .description("desc")
                .category("Science")
                .price(80.0)
                .pdfUrl("https://cdn.example.com/unsold.pdf")
                .previewImageUrl("https://cdn.example.com/unsold.png")
                .seller(seller)
                .build();
        note = noteRepository.save(note);

        Long salesCount = noteRepository.countSalesByNoteId(note.getId());

        assertThat(salesCount).isZero();
    }
}
