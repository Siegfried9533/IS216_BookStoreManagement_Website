package com.rgbunny.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Default;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Book")
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Title không được để trống!")
    @Size(max = 255, message = "Title không được vượt quá 255 ký tự!")
    @Column(name = "title")
    private String title;

    @NotBlank(message = "Author không được để trống!")
    @Size(max = 255, message = "Author không được vượt quá 255 ký tự!")
    @Column(name = "author")
    private String author;

    @NotBlank(message = "Description không được để trống!")
    @Size(max = 1000, message = "Description không được vượt quá 1000 ký tự!")
    @Column(name = "description")
    private String description;

    @NotBlank(message = "Category không được để trống!")
    @Size(max = 100, message = "Category không được vượt quá 100 ký tự!")
    @Column(name = "category")
    private String category;

    @NotNull(message = "Price không được để trống!")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price phải lớn hơn 0!")
    @Column(name = "price")
    private Float price;

    @NotBlank(message = "Publisher không được để trống!")
    @Size(max = 255, message = "Publisher không được vượt quá 255 ký tự!")
    @Column(name = "publisher")
    private String publisher;

    @NotNull(message = "Publication Date không được để trống!")
    @PastOrPresent(message = "Publication Date phải là ngày quá khứ hoặc hiện tại!")
    @Column(name = "publicationDate")
    private LocalDate publicationDate;

    @NotBlank(message = "Language không được để trống!")
    @Size(max = 100, message = "Language không được vượt quá 100 ký tự!")
    @Column(name = "language")
    private String language;

    @NotNull(message = "Reading Age không được để trống!")
    @Min(value = 0, message = "Reading Age không được nhỏ hơn 0!")
    @Column(name = "readingAge")
    private Integer readingAge;

    @NotNull(message = "Pages không được để trống!")
    @Min(value = 1, message = "Pages phải có ít nhất 1 trang!")
    @Column(name = "pages")
    private Integer pages;

    @Size(max = 50, message = "Dimension không được vượt quá 50 ký tự!")
    @Column(name = "dimension")
    private String dimension;

    @Column(name = "quantity")
    private Integer quantity = 0;

    @Column(name = "discount")
    private Double discount = 0.0;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "soldQuantity")
    private Integer soldQuantity = 0;

    @Column(name = "createdAt", insertable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "book", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private List<CartItem> books = new ArrayList<>();
}
