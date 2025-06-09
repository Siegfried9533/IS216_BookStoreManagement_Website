package com.rgbunny.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
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
    private Integer quantity;
    private Double discount;
    private String imageUrl;
    private Double rating;
    private Integer soldQuantity;
    private LocalDateTime createdAt;
}
