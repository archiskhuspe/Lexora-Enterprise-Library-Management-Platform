package com.library.management.service;

import com.library.management.dto.LoanDTO;
import com.library.management.entity.Book;
import com.library.management.entity.Loan;
import com.library.management.entity.Member;
import com.library.management.repository.BookRepository;
import com.library.management.repository.LoanRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.service.impl.LoanServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class LoanServiceTest {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Book testBook;
    private Member testMember;
    private Loan testLoan;
    private LoanDTO testLoanDTO;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAvailableCopies(1);

        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("Test Member");
        testMember.setActive(true);

        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setBook(testBook);
        testLoan.setMember(testMember);
        testLoan.setBorrowDate(LocalDateTime.now());
        testLoan.setExpectedReturnDate(LocalDateTime.now().plusDays(14));
        testLoan.setStatus(Loan.LoanStatus.ACTIVE);

        testLoanDTO = new LoanDTO();
        testLoanDTO.setId(1L);
        testLoanDTO.setBookId(1L);
        testLoanDTO.setMemberId(1L);
        testLoanDTO.setBookTitle("Test Book");
        testLoanDTO.setMemberName("Test Member");
        testLoanDTO.setBorrowDate(LocalDateTime.now());
        testLoanDTO.setExpectedReturnDate(LocalDateTime.now().plusDays(14));
        testLoanDTO.setStatus(Loan.LoanStatus.ACTIVE);
    }

    @Test
    void borrowBook_ValidRequest_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(loanRepository.findByMemberIdAndStatus(1L, Loan.LoanStatus.ACTIVE)).thenReturn(Arrays.asList());
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        LoanDTO result = loanService.borrowBook(testLoanDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookRepository).save(testBook);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void borrowBook_MemberNotFound_ThrowsException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> loanService.borrowBook(testLoanDTO));
        assertEquals("Member not found with ID: 1", exception.getMessage());
    }

    @Test
    void borrowBook_BookNotFound_ThrowsException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> loanService.borrowBook(testLoanDTO));
        assertEquals("Book not found with ID: 1", exception.getMessage());
    }

    @Test
    void borrowBook_InactiveMember_ThrowsException() {
        testMember.setActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> loanService.borrowBook(testLoanDTO));
        assertEquals("Member is not active", exception.getMessage());
    }

    @Test
    void borrowBook_NoAvailableCopies_ThrowsException() {
        testBook.setAvailableCopies(0);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> loanService.borrowBook(testLoanDTO));
        assertEquals("No available copies of the book", exception.getMessage());
    }

    @Test
    void returnBook_ValidRequest_Success() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        LoanDTO result = loanService.returnBook(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2, testBook.getAvailableCopies());
        assertEquals(Loan.LoanStatus.RETURNED, testLoan.getStatus());
        verify(bookRepository).save(testBook);
        verify(loanRepository).save(testLoan);
    }

    @Test
    void returnBook_LoanNotFound_ThrowsException() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> loanService.returnBook(1L));
        assertEquals("Loan not found with ID: 1", exception.getMessage());
    }

    @Test
    void returnBook_AlreadyReturned_ThrowsException() {
        testLoan.setStatus(Loan.LoanStatus.RETURNED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> loanService.returnBook(1L));
        assertEquals("Loan is already returned", exception.getMessage());
    }

    @Test
    void getLoansByMember_ValidRequest_Success() {
        Page<Loan> loanPage = new PageImpl<>(Arrays.asList(testLoan));
        Pageable pageable = PageRequest.of(0, 10);

        when(memberRepository.existsById(1L)).thenReturn(true);
        when(loanRepository.findByMemberId(1L, pageable)).thenReturn(loanPage);

        Page<LoanDTO> result = loanService.getLoansByMember(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testLoan.getId(), result.getContent().get(0).getId());
        assertEquals(testLoan.getBook().getTitle(), result.getContent().get(0).getBookTitle());
        assertEquals(testLoan.getMember().getName(), result.getContent().get(0).getMemberName());
    }

    @Test
    void getLoansByMember_MemberNotFound_ThrowsException() {
        when(memberRepository.existsById(1L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> loanService.getLoansByMember(1L, PageRequest.of(0, 10)));
        assertEquals("Member not found with ID: 1", exception.getMessage());
    }
} 