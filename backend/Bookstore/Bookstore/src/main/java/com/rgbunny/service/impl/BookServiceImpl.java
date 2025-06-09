package com.rgbunny.service.impl;

import com.rgbunny.dtos.BookResponse;
import com.rgbunny.model.Book;
import com.rgbunny.repository.BookRepository;
import com.rgbunny.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Override
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getFeaturedBooks() {
        // Lấy 8 sách nổi bật (có thể dựa vào rating hoặc số lượng bán)
        Pageable pageable = PageRequest.of(0, 8, Sort.by("rating").descending());
        List<Book> books = bookRepository.findAll(pageable).getContent();
        return books.stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getNewArrivals() {
        // Lấy 8 sách mới nhất
        Pageable pageable = PageRequest.of(0, 8, Sort.by("createdAt").descending());
        List<Book> books = bookRepository.findAll(pageable).getContent();
        return books.stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getBestSellers() {
        // Lấy 8 sách bán chạy nhất
        Pageable pageable = PageRequest.of(0, 8, Sort.by("soldQuantity").descending());
        List<Book> books = bookRepository.findAll(pageable).getContent();
        return books.stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return convertToBookResponse(book);
    }

    @Override
    public BookResponse createBook(Book book) {
        Book savedBook = bookRepository.save(book);
        return convertToBookResponse(savedBook);
    }

    @Override
    public BookResponse updateBook(Long id, Book book) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Cập nhật các trường
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setDescription(book.getDescription());
        existingBook.setPrice(book.getPrice());
        existingBook.setImageUrl(book.getImageUrl());
        existingBook.setCategory(book.getCategory());
        existingBook.setPublisher(book.getPublisher());
        existingBook.setPublicationDate(book.getPublicationDate());
        existingBook.setLanguage(book.getLanguage());
        existingBook.setReadingAge(book.getReadingAge());
        existingBook.setPages(book.getPages());
        existingBook.setDimension(book.getDimension());
        existingBook.setQuantity(book.getQuantity());
        existingBook.setDiscount(book.getDiscount());
        existingBook.setRating(book.getRating());
        existingBook.setSoldQuantity(book.getSoldQuantity());

        Book updatedBook = bookRepository.save(existingBook);
        return convertToBookResponse(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public Page<Book> searchBooks(String searchTerm, Map<String, String> filters, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort != null ? sort : "title"));
        // TODO: Implement search logic with filters
        return bookRepository.findAll(pageable);
    }

    private BookResponse convertToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .price(book.getPrice())
                .imageUrl(book.getImageUrl())
                .category(book.getCategory())
                .publisher(book.getPublisher())
                .publicationDate(book.getPublicationDate())
                .language(book.getLanguage())
                .readingAge(book.getReadingAge())
                .pages(book.getPages())
                .dimension(book.getDimension())
                .quantity(book.getQuantity())
                .discount(book.getDiscount())
                .rating(book.getRating())
                .soldQuantity(book.getSoldQuantity())
                .createdAt(book.getCreatedAt())
                .build();
    }
}