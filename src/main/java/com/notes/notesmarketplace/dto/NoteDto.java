package com.notes.notesmarketplace.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteDto {

    private Long id;

    private String title;

    private String description;

    private String category;

    private Double price;

    private String previewImageUrl;

}
