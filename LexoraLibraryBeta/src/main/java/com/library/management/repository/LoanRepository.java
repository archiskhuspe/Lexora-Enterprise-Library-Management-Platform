package com.library.management.repository;

import com.library.management.entity.Loan;
import com.library.management.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {
    Optional<Loan> findByIdAndMemberId(Long id, Long memberId);
    
    List<Loan> findByMemberIdAndStatus(Long memberId, Loan.LoanStatus status);
    
    @Query("SELECT l FROM Loan l WHERE l.expectedReturnDate < :now AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoans(@Param("now") LocalDateTime now);
    
    Page<Loan> findByMemberId(Long memberId, Pageable pageable);
    
    Page<Loan> findByBookId(Long bookId, Pageable pageable);
    
    @Query("SELECT l FROM Loan l WHERE l.status = :status")
    Page<Loan> findByStatus(@Param("status") Loan.LoanStatus status, Pageable pageable);
    
    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.status = 'ACTIVE'")
    List<Loan> findActiveLoansForBook(@Param("bookId") Long bookId);
} 