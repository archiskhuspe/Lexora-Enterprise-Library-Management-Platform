package com.library.management.controller;

import com.library.management.dto.BookDTO;
import com.library.management.dto.request.BookSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import com.library.management.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book Management", description = "APIs for managing library books")
public class BookController extends BaseController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @Operation(summary = "Add a new book")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) {
        return created(bookService.createBook(bookDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return ok(bookService.getBookById(id));
    }

    @GetMapping
    @Operation(summary = "Get all books with pagination")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<BookDTO>> getAllBooks(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return ok(bookService.getBookListWithFilters(pageNo, pageSize, null));
    }

    @Transactional(readOnly = true)
    @PostMapping("/list")
    @Operation(summary = "Get all books with filters and pagination")
    public ResponseEntity<PaginatedResponse<BookDTO>> getBookListWithFilters(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody(required = false) BookSearchRequest searchRequest) {
        return ok(bookService.getBookListWithFilters(pageNo, pageSize, searchRequest));
    }

    @GetMapping("/search")
    @Operation(summary = "Search books with filters")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<BookDTO>> searchBooks(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "available", required = false) Boolean available,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        BookSearchRequest searchRequest = new BookSearchRequest();
        searchRequest.setTitle(title);
        searchRequest.setAuthor(author);
        searchRequest.setCategory(category);
        searchRequest.setAvailable(available);
        return ok(bookService.getBookListWithFilters(pageNo, pageSize, searchRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update book details")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO bookDTO) {
        return ok(bookService.updateBook(id, bookDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
} 