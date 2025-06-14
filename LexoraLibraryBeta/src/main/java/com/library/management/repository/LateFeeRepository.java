package com.library.management.repository;

import com.library.management.entity.LateFee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LateFeeRepository extends JpaRepository<LateFee, Long> {
    Optional<LateFee> findByLoanId(Long loanId);
    
    Page<LateFee> findByLoanId(Long loanId, Pageable pageable);
    
    Page<LateFee> findByStatus(LateFee.FeeStatus status, Pageable pageable);
    
    @Query("SELECT lf FROM LateFee lf WHERE lf.loan.member.id = :memberId")
    Page<LateFee> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);
    
    @Query("SELECT lf FROM LateFee lf WHERE lf.status = 'PENDING' AND lf.createdAt < :date")
    List<LateFee> findUnpaidFeesOlderThan(@Param("date") LocalDateTime date);
} 