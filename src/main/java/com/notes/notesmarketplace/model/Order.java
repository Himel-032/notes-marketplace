package com.notes.notesmarketplace.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transactionId;

    private String orderNumber;

    private Double totalPrice;

    private String status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

}
