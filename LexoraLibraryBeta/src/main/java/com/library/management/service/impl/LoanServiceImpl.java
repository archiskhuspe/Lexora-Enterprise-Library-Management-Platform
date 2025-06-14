package com.library.management.service.impl;

import com.library.management.dto.LoanDTO;
import com.library.management.dto.request.LoanSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import com.library.management.entity.Book;
import com.library.management.entity.Loan;
import com.library.management.entity.Member;
import com.library.management.repository.BookRepository;
import com.library.management.repository.LoanRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.service.LoanService;
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

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {
    private static final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);
    private static final int LOAN_PERIOD_DAYS = 14;
    private static final int MAX_ACTIVE_LOANS = 5;

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanServiceImpl(LoanRepository loanRepository, BookRepository bookRepository, MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public LoanDTO borrowBook(LoanDTO loanDTO) {
        logger.info("Processing book borrow request for book ID: {} and member ID: {}", 
                   loanDTO.getBookId(), loanDTO.getMemberId());

        Member member = memberRepository.findById(loanDTO.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found with ID: " + loanDTO.getMemberId()));

        if (!member.isActive()) {
            throw new IllegalStateException("Member is not active");
        }

        Book book = bookRepository.findById(loanDTO.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + loanDTO.getBookId()));

        validateLoanCreation(book, member);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowDate(LocalDateTime.now());
        loan.setExpectedReturnDate(loan.getBorrowDate().plusDays(LOAN_PERIOD_DAYS));
        loan.setStatus(Loan.LoanStatus.ACTIVE);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan savedLoan = loanRepository.save(loan);
        logger.info("Book borrowed successfully. Loan ID: {}", savedLoan.getId());

        return convertToDTO(savedLoan);
    }

    @Override
    @Transactional
    public LoanDTO returnBook(Long loanId) {
        logger.info("Processing book return for loan ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found with ID: " + loanId));

        if (loan.getStatus() == Loan.LoanStatus.RETURNED) {
            throw new IllegalStateException("Loan is already returned");
        }

        loan.setActualReturnDate(LocalDateTime.now());
        loan.setStatus(Loan.LoanStatus.RETURNED);

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        Loan updatedLoan = loanRepository.save(loan);
        logger.info("Book returned successfully for loan ID: {}", loanId);

        return convertToDTO(updatedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDTO getLoanById(Long id) {
        logger.debug("Fetching loan with ID: {}", id);
        return loanRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanDTO> getLoansByMember(Long memberId, Pageable pageable) {
        logger.debug("Fetching loans for member ID: {}", memberId);

        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException("Member not found with ID: " + memberId);
        }

        return loanRepository.findByMemberId(memberId, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanDTO> getLoansByBook(Long bookId, Pageable pageable) {
        logger.debug("Fetching loans for book ID: {}", bookId);

        if (!bookRepository.existsById(bookId)) {
            throw new EntityNotFoundException("Book not found with ID: " + bookId);
        }

        return loanRepository.findByBookId(bookId, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void updateOverdueLoans() {
        logger.info("Updating overdue loans");
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDateTime.now());
        
        for (Loan loan : overdueLoans) {
            loan.setStatus(Loan.LoanStatus.OVERDUE);
            loanRepository.save(loan);
            logger.debug("Marked loan ID: {} as overdue", loan.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canMemberBorrow(Long memberId) {
        return getActiveLoansCount(memberId) < MAX_ACTIVE_LOANS;
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveLoansCount(Long memberId) {
        return loanRepository.findByMemberIdAndStatus(memberId, Loan.LoanStatus.ACTIVE).size();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<LoanDTO> getLoansByMember(Long memberId, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<Loan> loans = loanRepository.findByMemberId(memberId, pageable);
        Page<LoanDTO> loanDTOs = loans.map(this::convertToDTO);
        return PaginatedResponse.from(loanDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<LoanDTO> getLoansByBook(Long bookId, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<Loan> loans = loanRepository.findByBookId(bookId, pageable);
        Page<LoanDTO> loanDTOs = loans.map(this::convertToDTO);
        return PaginatedResponse.from(loanDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<LoanDTO> getAllLoans(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<Loan> loans = loanRepository.findAll(pageable);
        Page<LoanDTO> loanDTOs = loans.map(this::convertToDTO);
        return PaginatedResponse.from(loanDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<LoanDTO> getLoanListWithFilters(Integer pageNo, Integer pageSize, LoanSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        
        Specification<Loan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (searchRequest != null) {
                if (searchRequest.getMemberId() != null) {
                    predicates.add(cb.equal(root.get("member").get("id"), searchRequest.getMemberId()));
                }
                
                if (searchRequest.getBookId() != null) {
                    predicates.add(cb.equal(root.get("book").get("id"), searchRequest.getBookId()));
                }
                
                if (searchRequest.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), searchRequest.getStatus()));
                }
                
                if (searchRequest.getBorrowDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("borrowDate"), searchRequest.getBorrowDateFrom()));
                }
                
                if (searchRequest.getBorrowDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("borrowDate"), searchRequest.getBorrowDateTo()));
                }
                
                if (searchRequest.getReturnDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("actualReturnDate"), searchRequest.getReturnDateFrom()));
                }
                
                if (searchRequest.getReturnDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("actualReturnDate"), searchRequest.getReturnDateTo()));
                }
                
                if (searchRequest.getOverdue() != null && searchRequest.getOverdue()) {
                    predicates.add(cb.lessThan(root.get("expectedReturnDate"), LocalDateTime.now()));
                    predicates.add(cb.equal(root.get("status"), Loan.LoanStatus.ACTIVE));
                }
            }
            
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Loan> loans = loanRepository.findAll(spec, pageable);
        Page<LoanDTO> loanDTOs = loans.map(this::convertToDTO);
        return PaginatedResponse.from(loanDTOs);
    }

    private void validateLoanCreation(Book book, Member member) {
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies of the book");
        }

        if (!canMemberBorrow(member.getId())) {
            throw new IllegalStateException("Member has reached maximum number of active loans");
        }
    }

    private LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setBookId(loan.getBook().getId());
        dto.setMemberId(loan.getMember().getId());
        dto.setBorrowDate(loan.getBorrowDate());
        dto.setExpectedReturnDate(loan.getExpectedReturnDate());
        dto.setActualReturnDate(loan.getActualReturnDate());
        dto.setStatus(loan.getStatus());
        dto.setBookTitle(loan.getBook().getTitle());
        dto.setMemberName(loan.getMember().getName());
        return dto;
    }
} 