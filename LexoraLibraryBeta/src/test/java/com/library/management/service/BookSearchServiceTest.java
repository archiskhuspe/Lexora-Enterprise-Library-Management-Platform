package com.library.management.service;

import com.library.management.dto.BookDTO;
import com.library.management.entity.Book;
import com.library.management.repository.BookRepository;
import com.library.management.service.impl.BookSearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookSearchServiceImpl bookSearchService;

    private Book testBook;
    private Page<Book> bookPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setCategory("Test Category");
        testBook.setPublicationYear(2024);
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(3);

        pageable = PageRequest.of(0, 10);
        bookPage = new PageImpl<>(Collections.singletonList(testBook));
    }

    @Test
    void searchBooks_Success() {
        // Arrange
        String query = "test";
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(bookPage);

        // Act
        Page<BookDTO> result = bookSearchService.searchBooks(query, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBook.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    void searchByTitle_Success() {
        // Arrange
        String title = "Test";
        when(bookRepository.findByTitleContainingIgnoreCase(eq(title), any(Pageable.class))).thenReturn(bookPage);

        // Act
        Page<BookDTO> result = bookSearchService.searchByTitle(title, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBook.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    void searchByAuthor_Success() {
        // Arrange
        String author = "Test";
        when(bookRepository.findByAuthorContainingIgnoreCase(eq(author), any(Pageable.class))).thenReturn(bookPage);

        // Act
        Page<BookDTO> result = bookSearchService.searchByAuthor(author, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBook.getAuthor(), result.getContent().get(0).getAuthor());
    }

    @Test
    void searchByCategory_Success() {
        // Arrange
        String category = "Test Category";
        when(bookRepository.findByCategoryContainingIgnoreCase(eq(category), any(Pageable.class))).thenReturn(bookPage);

        // Act
        Page<BookDTO> result = bookSearchService.searchByCategory(category, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBook.getCategory(), result.getContent().get(0).getCategory());
    }

    @Test
    void searchByAvailability_Available_Success() {
        // Arrange
        when(bookRepository.findAvailableBooks(any(Pageable.class))).thenReturn(bookPage);

        // Act
        Page<BookDTO> result = bookSearchService.searchByAvailability(true, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBook.getAvailableCopies(), result.getContent().get(0).getAvailableCopies());
    }

    @Test
    void searchByAvailability_All_Success() {
        // Arrange
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        // Act
        Page<BookDTO> result = bookSearchService.searchByAvailability(false, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAllBooks_Success() {
        // Arrange
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        // Act
        Page<BookDTO> result = bookSearchService.findAllBooks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBook.getTitle(), result.getContent().get(0).getTitle());
    }
} 