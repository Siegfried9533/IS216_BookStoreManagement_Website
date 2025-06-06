package com.rgbunny.service;

import com.rgbunny.repository.BookRepository;
import com.rgbunny.dtos.BookResponse;
import com.rgbunny.model.Book;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Path;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;
import org.springframework.ui.ModelMap;

@Service
public class BookServiceImp implements BookService {
    @Autowired
    private final BookRepository bookRepository;

    @Autowired
    ModelMapper modelMapper;

    public BookServiceImp(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(book -> {
            return modelMapper.map(book, BookResponse.class);
        }).toList();
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException());
        BookResponse bookResponse = modelMapper.map(book, BookResponse.class);
        return bookResponse;
    }

    @Override
    public BookResponse createBook(Book book) {
        if (bookRepository.existsByTitle(book.getTitle()))
            throw new IllegalArgumentException("Title đã tồn tại");
        bookRepository.save(book);
        return modelMapper.map(book, BookResponse.class);
    }

    @Override
    public BookResponse updateBook(Long id, Book updated) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Book với id = " + id));

        if (updated.getTitle() == null || updated.getTitle().equals(existing.getTitle())) {
            if (updated.getAuthor() != null) {
                existing.setAuthor(updated.getAuthor());
            }
            if (updated.getDescription() != null)
                existing.setDescription(updated.getDescription());

            if (updated.getCategory() != null)
                existing.setCategory(updated.getCategory());
            if (updated.getPrice() != null)
                existing.setPrice(updated.getPrice());
            if (updated.getPublisher() != null)
                existing.setPublisher(updated.getPublisher());
            if (updated.getPublicationDate() != null)
                existing.setPublicationDate(updated.getPublicationDate());
            if (updated.getLanguage() != null)
                existing.setLanguage(updated.getLanguage());
            if (updated.getReadingAge() != null)
                existing.setReadingAge(updated.getReadingAge());
            if (updated.getPages() != null)
                existing.setPages(updated.getPages());
            if (updated.getDimension() != null)
                existing.setDimension(updated.getDimension());
            Book book = bookRepository.save(existing);
            return modelMapper.map(book, BookResponse.class);
        } else if (bookRepository.existsByTitle(updated.getTitle())) {
            throw new IllegalArgumentException("Title đã tồn tại");
        }

        existing.setTitle(updated.getTitle());

        Book book = bookRepository.save(existing);

        return modelMapper.map(book, BookResponse.class);
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy Book với id = " + id);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public Page<Book> searchBooks(String searchTerm, Map<String, String> filters, String sort, int page, int size) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Specification<Book> spec = buildSpecification(searchTerm, filters);
        return bookRepository.findAll(spec, pageable);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Tham số sort không hợp lệ!");
        }
        String property = parts[0].trim();
        String direction = parts[1].trim();
        return Sort.by(Sort.Direction.fromString(direction), property);
    }

    private Specification<Book> buildSpecification(String searchTerm, Map<String, String> filters) {
        return Specification.where(createSearchSpec(searchTerm)).and(createFilterSpec(filters));
    }

    private Specification<Book> createSearchSpec(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return null;
            }
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("author")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("category")), pattern),
                    cb.like(cb.lower(root.get("publisher")), pattern),
                    cb.like(cb.lower(root.get("language")), pattern));
        };
    }

    private Specification<Book> createFilterSpec(Map<String, String> filters) {
        return (root, query, cb) -> {
            if (filters == null || filters.isEmpty()) {
                return null;
            }
            List<Predicate> predicates = new ArrayList<>();
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                String[] parts = key.split("_");
                if (parts.length == 0)
                    continue;
                String field = parts[0];
                String operator = parts.length > 1 ? parts[1] : "eq";

                Path<?> path = root.get(field);
                Class<?> type = path.getJavaType();

                Object typedValue;
                try {
                    if (type == String.class) {
                        typedValue = value;
                    } else if (type == Float.class) {
                        typedValue = Float.parseFloat(value);
                    } else if (type == LocalDate.class) {
                        typedValue = LocalDate.parse(value);
                    } else if (type == Integer.class) {
                        typedValue = Integer.parseInt(value);
                    } else {
                        throw new IllegalArgumentException("Trường " + field + " không hỗ trợ!");
                    }
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Giá trị ngày không hợp lệ cho trường " + field + ": " + value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Giá trị số không hợp lệ cho trường " + field + ": " + value);
                }

                switch (operator.toLowerCase()) {
                    case "eq":
                        predicates.add(cb.equal(path, typedValue));
                        break;
                    case "neq":
                        predicates.add(cb.notEqual(path, typedValue));
                        break;
                    case "gte":
                        if (typedValue instanceof Number) {
                            predicates.add(cb.ge((Path<Number>) path, (Number) typedValue));
                        } else if (typedValue instanceof LocalDate) {
                            predicates.add(cb.greaterThanOrEqualTo((Path<LocalDate>) path, (LocalDate) typedValue));
                        }
                        break;
                    case "lte":
                        if (typedValue instanceof Number) {
                            predicates.add(cb.le((Path<Number>) path, (Number) typedValue));
                        } else if (typedValue instanceof LocalDate) {
                            predicates.add(cb.lessThanOrEqualTo((Path<LocalDate>) path, (LocalDate) typedValue));
                        }
                        break;
                    case "gt":
                        if (typedValue instanceof Number) {
                            predicates.add(cb.gt((Path<Number>) path, (Number) typedValue));
                        } else if (typedValue instanceof LocalDate) {
                            predicates.add(cb.greaterThan((Path<LocalDate>) path, (LocalDate) typedValue));
                        }
                        break;
                    case "lt":
                        if (typedValue instanceof Number) {
                            predicates.add(cb.lt((Path<Number>) path, (Number) typedValue));
                        } else if (typedValue instanceof LocalDate) {
                            predicates.add(cb.lessThan((Path<LocalDate>) path, (LocalDate) typedValue));
                        }
                        break;
                    case "like":
                        predicates.add(cb.like(path.as(String.class), "%" + value + "%"));
                        break;
                    default:
                        throw new IllegalArgumentException("Toán tử không hỗ trợ: " + operator);
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
