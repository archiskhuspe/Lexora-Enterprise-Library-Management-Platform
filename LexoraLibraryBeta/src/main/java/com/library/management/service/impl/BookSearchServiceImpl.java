package com.library.management.service.impl;

import com.library.management.dto.BookDTO;
import com.library.management.entity.Book;
import com.library.management.repository.BookRepository;
import com.library.management.service.BookSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookSearchServiceImpl implements BookSearchService {
    private static final Logger logger = LoggerFactory.getLogger(BookSearchServiceImpl.class);
    private final BookRepository bookRepository;

    public BookSearchServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> searchBooks(String query, Pageable pageable) {
        logger.debug("Searching books with query: {}", query);
        return bookRepository.findAll(createSearchSpecification(query), pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> searchByTitle(String title, Pageable pageable) {
        logger.debug("Searching books by title: {}", title);
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> searchByAuthor(String author, Pageable pageable) {
        logger.debug("Searching books by author: {}", author);
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> searchByCategory(String category, Pageable pageable) {
        logger.debug("Searching books by category: {}", category);
        return bookRepository.findByCategoryContainingIgnoreCase(category, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> searchByAvailability(boolean available, Pageable pageable) {
        logger.debug("Searching books by availability: {}", available);
        if (available) {
            return bookRepository.findAvailableBooks(pageable)
                    .map(this::convertToDTO);
        }
        return findAllBooks(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> findAllBooks(Pageable pageable) {
        logger.debug("Fetching all books");
        return bookRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    private Specification<Book> createSearchSpecification(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String searchTerm = "%" + query.toLowerCase() + "%";

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchTerm));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), searchTerm));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), searchTerm));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("isbn")), searchTerm));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setCategory(book.getCategory());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setTotalCopies(book.getTotalCopies());
        dto.setAvailableCopies(book.getAvailableCopies());
        return dto;
    }
} 