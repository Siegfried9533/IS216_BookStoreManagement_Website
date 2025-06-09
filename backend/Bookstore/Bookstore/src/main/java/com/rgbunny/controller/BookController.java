package com.rgbunny.controller;

import com.rgbunny.dtos.BookResponse;
import com.rgbunny.model.Book;
import com.rgbunny.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/api/books")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookController {

    @Autowired
    BookService bookService;

    @GetMapping()
    public ResponseEntity<Map<String, Object>> getAllBooks() {
        try {
            List<BookResponse> books = bookService.getAllBooks();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", books));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedBooks() {
        try {
            List<BookResponse> books = bookService.getFeaturedBooks();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", books));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<Map<String, Object>> getNewArrivals() {
        try {
            List<BookResponse> books = bookService.getNewArrivals();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", books));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<Map<String, Object>> getBestSellers() {
        try {
            List<BookResponse> books = bookService.getBestSellers();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", books));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("/book")
    public ResponseEntity<Map<String, Object>> getBookById(@RequestParam Long Id) {
        try {
            BookResponse book = bookService.getBookById(Id);
            if (book == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", "error",
                                "message", "Book not found"));
            }
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", book));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()));
        }
    }

    @PostMapping()
    public ResponseEntity<Map<String, Object>> createBook(@RequestBody @Valid Book book) {
        try {
            BookResponse createdBook = bookService.createBook(book);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", createdBook));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()));
        }
    }

    @PatchMapping()
    public ResponseEntity<Map<String, Object>> updateBook(@RequestParam Long id, @RequestBody Book book) {
        try {
            BookResponse updatedBook = bookService.updateBook(id, book);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", updatedBook));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()));
        }
    }

    @DeleteMapping()
    public ResponseEntity<Map<String, Object>> deleteBook(@RequestParam Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Book deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchBooks(
            @RequestParam(required = false) String searchTerm,
            @RequestParam Map<String, String> allParams,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size) {
        try {
            Map<String, String> filters = allParams.entrySet().stream()
                    .filter(e -> e.getKey().startsWith("filter_"))
                    .collect(Collectors.toMap(
                            e -> e.getKey().substring("filter_".length()),
                            Map.Entry::getValue));

            Page<Book> books = bookService.searchBooks(searchTerm, filters, sort, page, size);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", books));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()));
        }
    }
}
