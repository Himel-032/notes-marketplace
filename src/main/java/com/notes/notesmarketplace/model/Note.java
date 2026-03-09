package com.notes.notesmarketplace.model;


import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String category;

    private Double price;

    @Column(nullable = false)
    private String pdfUrl;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
}
