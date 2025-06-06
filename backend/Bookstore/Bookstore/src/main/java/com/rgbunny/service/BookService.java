package com.rgbunny.service;

import com.rgbunny.dtos.BookResponse;
import com.rgbunny.model.Book;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BookService {
    List<BookResponse> getAllBooks();

    BookResponse getBookById(Long id);

    BookResponse createBook(Book book);

    BookResponse updateBook(Long id, Book book);

    void deleteBook(Long id);

    Page<Book> searchBooks(String searchTerm, Map<String, String> filters, String sort, int page, int size);
}
