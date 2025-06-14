package com.library.management.service;

import com.library.management.dto.BookDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookSearchService {
    Page<BookDTO> searchBooks(String query, Pageable pageable);
    Page<BookDTO> searchByTitle(String title, Pageable pageable);
    Page<BookDTO> searchByAuthor(String author, Pageable pageable);
    Page<BookDTO> searchByCategory(String category, Pageable pageable);
    Page<BookDTO> searchByAvailability(boolean available, Pageable pageable);
    Page<BookDTO> findAllBooks(Pageable pageable);
} 