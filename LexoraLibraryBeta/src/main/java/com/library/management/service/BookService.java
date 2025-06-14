package com.library.management.service;

import com.library.management.dto.BookDTO;
import com.library.management.dto.request.BookSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDTO createBook(BookDTO bookDTO);
    BookDTO getBookById(Long id);
    BookDTO getBookByIsbn(String isbn);
    Page<BookDTO> getAllBooks(Pageable pageable);
    Page<BookDTO> searchBooks(String title, String author, String category, Boolean available, Pageable pageable);
    Page<BookDTO> getAvailableBooks(Pageable pageable);
    BookDTO updateBook(Long id, BookDTO bookDTO);
    void deleteBook(Long id);
    boolean isIsbnUnique(String isbn);
    PaginatedResponse<BookDTO> getBookListWithFilters(Integer pageNo, Integer pageSize, BookSearchRequest searchRequest);
} 