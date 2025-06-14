package com.library.management.service.impl;

import com.library.management.dto.LateFeeDTO;
import com.library.management.entity.LateFee;
import com.library.management.entity.Loan;
import com.library.management.repository.LateFeeRepository;
import com.library.management.repository.LoanRepository;
import com.library.management.service.LateFeeService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LateFeeServiceImpl implements LateFeeService {
    private static final Logger logger = LoggerFactory.getLogger(LateFeeServiceImpl.class);
    private static final BigDecimal DAILY_LATE_FEE_RATE = new BigDecimal("1.00"); // $1 per day

    private final LateFeeRepository lateFeeRepository;
    private final LoanRepository loanRepository;

    public LateFeeServiceImpl(LateFeeRepository lateFeeRepository, LoanRepository loanRepository) {
        this.lateFeeRepository = lateFeeRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    @Transactional
    public LateFeeDTO calculateLateFee(Long loanId) {
        logger.info("Calculating late fee for loan ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found with ID: " + loanId));

        if (loan.getStatus() != Loan.LoanStatus.OVERDUE) {
            throw new IllegalStateException("Cannot calculate late fee for non-overdue loan");
        }

        // Check if late fee already exists
        if (lateFeeRepository.findByLoanId(loanId).isPresent()) {
            throw new IllegalStateException("Late fee already exists for loan ID: " + loanId);
        }

        BigDecimal amount = calculateLateFeeAmount(loanId);
        int daysOverdue = (int) ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), LocalDateTime.now());

        LateFee lateFee = new LateFee();
        lateFee.setLoan(loan);
        lateFee.setAmount(amount);
        lateFee.setDaysOverdue(daysOverdue);
        lateFee.setStatus(LateFee.FeeStatus.PENDING);

        LateFee savedLateFee = lateFeeRepository.save(lateFee);
        logger.info("Late fee calculated and saved for loan ID: {}", loanId);

        return convertToDTO(savedLateFee);
    }

    @Override
    @Transactional
    public LateFeeDTO payLateFee(Long lateFeeId) {
        logger.info("Processing payment for late fee ID: {}", lateFeeId);

        LateFee lateFee = lateFeeRepository.findById(lateFeeId)
                .orElseThrow(() -> new EntityNotFoundException("Late fee not found with ID: " + lateFeeId));

        if (lateFee.getStatus() != LateFee.FeeStatus.PENDING) {
            throw new IllegalStateException("Late fee is not in PENDING status");
        }

        lateFee.setStatus(LateFee.FeeStatus.PAID);
        lateFee.setPaidDate(LocalDateTime.now());

        LateFee updatedLateFee = lateFeeRepository.save(lateFee);
        logger.info("Late fee paid successfully for ID: {}", lateFeeId);

        return convertToDTO(updatedLateFee);
    }

    @Override
    @Transactional(readOnly = true)
    public LateFeeDTO getLateFeeById(Long id) {
        logger.debug("Fetching late fee with ID: {}", id);
        return lateFeeRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Late fee not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LateFeeDTO> getLateFeesByLoan(Long loanId, Pageable pageable) {
        logger.debug("Fetching late fees for loan ID: {}", loanId);
        return lateFeeRepository.findByLoanId(loanId, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LateFeeDTO> getUnpaidFeesByMember(Long memberId) {
        logger.debug("Fetching unpaid late fees for member ID: {}", memberId);
        return lateFeeRepository.findByMemberId(memberId, Pageable.unpaged())
                .stream()
                .filter(lateFee -> lateFee.getStatus() == LateFee.FeeStatus.PENDING)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUnpaidFees(Long memberId) {
        return !getUnpaidFeesByMember(memberId).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateLateFeeAmount(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found with ID: " + loanId));

        if (loan.getExpectedReturnDate() == null) {
            throw new IllegalStateException("Loan has no expected return date");
        }

        LocalDateTime returnDate = loan.getActualReturnDate() != null ? 
                loan.getActualReturnDate() : LocalDateTime.now();
        
        long daysLate = ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), returnDate);
        
        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }

        return DAILY_LATE_FEE_RATE.multiply(new BigDecimal(daysLate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private LateFeeDTO convertToDTO(LateFee lateFee) {
        LateFeeDTO dto = new LateFeeDTO();
        dto.setId(lateFee.getId());
        dto.setLoanId(lateFee.getLoan().getId());
        dto.setAmount(lateFee.getAmount());
        dto.setDaysOverdue(lateFee.getDaysOverdue());
        dto.setPaidDate(lateFee.getPaidDate());
        dto.setStatus(lateFee.getStatus());
        dto.setMemberName(lateFee.getLoan().getMember().getName());
        dto.setBookTitle(lateFee.getLoan().getBook().getTitle());
        return dto;
    }
} 