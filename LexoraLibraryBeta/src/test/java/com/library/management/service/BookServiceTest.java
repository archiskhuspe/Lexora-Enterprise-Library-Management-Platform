package com.library.management.service;

import com.library.management.dto.BookDTO;
import com.library.management.entity.Book;
import com.library.management.entity.Loan;
import com.library.management.repository.BookRepository;
import com.library.management.repository.LoanRepository;
import com.library.management.service.impl.BookServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private AuditService auditService;

    private BookService bookService;
    private Book testBook;
    private BookDTO testBookDTO;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(bookRepository, loanRepository, auditService);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setCategory("Test Category");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(5);
        testBook.setPublicationYear(2024);

        testBookDTO = new BookDTO();
        testBookDTO.setTitle("Test Book");
        testBookDTO.setAuthor("Test Author");
        testBookDTO.setIsbn("1234567890");
        testBookDTO.setCategory("Test Category");
        testBookDTO.setTotalCopies(5);
        testBookDTO.setPublicationYear(2024);
    }

    @Test
    void createBook_Success() {
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        BookDTO result = bookService.createBook(testBookDTO);

        assertNotNull(result);
        assertEquals(testBookDTO.getTitle(), result.getTitle());
        assertEquals(testBookDTO.getAuthor(), result.getAuthor());
        verify(auditService).logAction("CREATE", "BOOK", 1L, "Created new book: Test Book");
    }

    @Test
    void getBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        BookDTO result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals(testBook.getTitle(), result.getTitle());
        assertEquals(testBook.getAuthor(), result.getAuthor());
    }

    @Test
    void getBookById_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.getBookById(1L));
    }

    @Test
    void getAllBooks_Success() {
        List<Book> books = new ArrayList<>();
        books.add(testBook);
        Page<Book> page = new PageImpl<>(books);
        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<BookDTO> result = bookService.getAllBooks(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testBook.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    void updateBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        BookDTO result = bookService.updateBook(1L, testBookDTO);

        assertNotNull(result);
        assertEquals(testBookDTO.getTitle(), result.getTitle());
        assertEquals(testBookDTO.getAuthor(), result.getAuthor());
        verify(auditService).logAction("UPDATE", "BOOK", 1L, "Updated book: Test Book");
    }

    @Test
    void deleteBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(loanRepository.findActiveLoansForBook(1L)).thenReturn(Collections.emptyList());

        bookService.deleteBook(1L);

        verify(bookRepository).delete(testBook);
        verify(auditService).logAction("DELETE", "BOOK", 1L, "Deleted book: Test Book");
    }

    @Test
    void deleteBook_HasActiveLoans_ThrowsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        List<Loan> activeLoans = new ArrayList<>();
        activeLoans.add(new Loan());
        when(loanRepository.findActiveLoansForBook(1L)).thenReturn(activeLoans);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookService.deleteBook(1L));
        assertEquals("Cannot delete book with active loans", exception.getMessage());
    }

    @Test
    void searchBooks_Success() {
        List<Book> books = new ArrayList<>();
        books.add(testBook);
        Page<Book> page = new PageImpl<>(books);
        when(bookRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<BookDTO> result = bookService.searchBooks("test", "author", "category", true,
                PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testBook.getTitle(), result.getContent().get(0).getTitle());
    }
} 