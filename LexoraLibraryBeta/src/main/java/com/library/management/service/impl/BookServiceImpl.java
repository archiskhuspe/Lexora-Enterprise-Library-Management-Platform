package com.library.management.service.impl;

import com.library.management.dto.BookDTO;
import com.library.management.dto.request.BookSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import com.library.management.entity.Book;
import com.library.management.repository.BookRepository;
import com.library.management.repository.LoanRepository;
import com.library.management.service.AuditService;
import com.library.management.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final AuditService auditService;

    public BookServiceImpl(BookRepository bookRepository, LoanRepository loanRepository, AuditService auditService) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public BookDTO createBook(BookDTO bookDTO) {
        logger.info("Creating new book with ISBN: {}", bookDTO.getIsbn());
        
        if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
        }

        Book book = convertToEntity(bookDTO);
        book.setAvailableCopies(bookDTO.getTotalCopies());
        Book savedBook = bookRepository.save(book);
        
        auditService.logAction("CREATE", "BOOK", savedBook.getId(), 
            "Created new book: " + savedBook.getTitle());
        
        logger.info("Book created successfully with ID: {}", savedBook.getId());
        return convertToDTO(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        logger.debug("Fetching book with ID: {}", id);
        return bookRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public BookDTO getBookByIsbn(String isbn) {
        logger.debug("Fetching book with ISBN: {}", isbn);
        return bookRepository.findByIsbn(isbn)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ISBN: " + isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        logger.debug("Fetching all books with pagination");
        return bookRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> searchBooks(String title, String author, String category, Boolean available, Pageable pageable) {
        logger.debug("Searching books with title: {}, author: {}, category: {}, available: {}", title, author, category, available);
        
        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            
            if (author != null && !author.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
            }
            
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%"));
            }
            
            if (available != null && available) {
                predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
            }
            
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        
        try {
            Page<Book> books = bookRepository.findAll(spec, pageable);
            return books.map(this::convertToDTO);
        } catch (Exception e) {
            logger.error("Error searching books: {}", e.getMessage());
            throw new RuntimeException("Error searching books", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> getAvailableBooks(Pageable pageable) {
        logger.debug("Fetching available books");
        return bookRepository.findByAvailableCopiesGreaterThan(0, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        logger.info("Updating book with ID: {}", id);
        
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + id));

        if (!existingBook.getIsbn().equals(bookDTO.getIsbn()) && 
            bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
        }

        updateBookEntity(existingBook, bookDTO);
        Book updatedBook = bookRepository.save(existingBook);
        
        auditService.logAction("UPDATE", "BOOK", updatedBook.getId(), 
            "Updated book: " + updatedBook.getTitle());
        
        logger.info("Book updated successfully with ID: {}", id);
        return convertToDTO(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        logger.info("Deleting book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + id));
        
        if (!loanRepository.findActiveLoansForBook(id).isEmpty()) {
            throw new IllegalStateException("Cannot delete book with active loans");
        }
        
        String bookTitle = book.getTitle();
        bookRepository.delete(book);
        
        auditService.logAction("DELETE", "BOOK", id, 
            "Deleted book: " + bookTitle);
        
        logger.info("Book deleted successfully with ID: {}", id);
    }

    @Override
    public boolean isIsbnUnique(String isbn) {
        return !bookRepository.existsByIsbn(isbn);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<BookDTO> getBookListWithFilters(Integer pageNo, Integer pageSize, BookSearchRequest searchRequest) {
        logger.debug("Fetching books with filters: {}", searchRequest);
        
        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (searchRequest != null) {
                if (searchRequest.getTitle() != null && !searchRequest.getTitle().trim().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + searchRequest.getTitle().toLowerCase() + "%"));
                }
                
                if (searchRequest.getAuthor() != null && !searchRequest.getAuthor().trim().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("author")), "%" + searchRequest.getAuthor().toLowerCase() + "%"));
                }
                
                if (searchRequest.getIsbn() != null && !searchRequest.getIsbn().trim().isEmpty()) {
                    predicates.add(cb.equal(root.get("isbn"), searchRequest.getIsbn()));
                }
                
                if (searchRequest.getCategory() != null && !searchRequest.getCategory().trim().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("category")), "%" + searchRequest.getCategory().toLowerCase() + "%"));
                }
                
                if (searchRequest.getPublicationYear() != null) {
                    predicates.add(cb.equal(root.get("publicationYear"), searchRequest.getPublicationYear()));
                }
                
                if (searchRequest.getAvailable() != null && searchRequest.getAvailable()) {
                    predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
                }
            }
            
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        
        try {
            Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
            Page<Book> books = bookRepository.findAll(spec, pageable);
            Page<BookDTO> bookDTOs = books.map(this::convertToDTO);
            return PaginatedResponse.from(bookDTOs);
        } catch (Exception e) {
            logger.error("Error fetching books with filters: {}", e.getMessage());
            throw new RuntimeException("Error fetching books", e);
        }
    }

    private Book convertToEntity(BookDTO bookDTO) {
        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setIsbn(bookDTO.getIsbn());
        book.setCategory(bookDTO.getCategory());
        book.setPublicationYear(bookDTO.getPublicationYear());
        book.setTotalCopies(bookDTO.getTotalCopies());
        book.setAvailableCopies(bookDTO.getTotalCopies());
        return book;
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

    private void updateBookEntity(Book book, BookDTO bookDTO) {
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setIsbn(bookDTO.getIsbn());
        book.setCategory(bookDTO.getCategory());
        book.setPublicationYear(bookDTO.getPublicationYear());
        
        // Update copies only if new value is provided
        if (bookDTO.getTotalCopies() != null) {
            int difference = bookDTO.getTotalCopies() - book.getTotalCopies();
            book.setTotalCopies(bookDTO.getTotalCopies());
            book.setAvailableCopies(book.getAvailableCopies() + difference);
        }
    }
} 