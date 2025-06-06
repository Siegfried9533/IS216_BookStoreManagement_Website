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
public class BookController {

    @Autowired
    BookService bookService;

    @GetMapping()
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBooks();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/book")
    public ResponseEntity<BookResponse> getBookById(@RequestParam Long Id) {
        BookResponse book = bookService.getBookById(Id);
        if (book == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @PostMapping()
    public BookResponse createBook(@RequestBody @Valid Book book) {
        return bookService.createBook(book);
    }

    @PatchMapping()
    public BookResponse updateBook(@RequestParam Long id, @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    @DeleteMapping()
    public void deleteBook(@RequestParam Long id) {
        bookService.deleteBook(id);
    }

    @GetMapping("/search")
    public Page<Book> searchBooks(@RequestParam(required = false) String searchTerm,
            @RequestParam Map<String, String> allParams,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size) {

        Map<String, String> filters = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("filter_"))
                .collect(Collectors.toMap(
                        e -> e.getKey().substring("filter_".length()),
                        Map.Entry::getValue));

        return bookService.searchBooks(searchTerm, filters, sort, page, size);
    }
}
