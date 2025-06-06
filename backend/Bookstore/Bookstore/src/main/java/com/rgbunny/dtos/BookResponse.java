package com.rgbunny.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String category;
    private Float price;
    private String publisher;
    private LocalDate publicationDate;
    private String language;
    private Integer readingAge;
    private Integer pages;
    private String dimension;
    private Integer quantity = 0;
    private Double discount = 0.0;
}
