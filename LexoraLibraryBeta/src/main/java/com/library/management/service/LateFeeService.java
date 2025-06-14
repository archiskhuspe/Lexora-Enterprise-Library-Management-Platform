package com.library.management.service;

import com.library.management.dto.LateFeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface LateFeeService {
    LateFeeDTO calculateLateFee(Long loanId);
    LateFeeDTO payLateFee(Long lateFeeId);
    LateFeeDTO getLateFeeById(Long id);
    Page<LateFeeDTO> getLateFeesByLoan(Long loanId, Pageable pageable);
    List<LateFeeDTO> getUnpaidFeesByMember(Long memberId);
    boolean hasUnpaidFees(Long memberId);
    BigDecimal calculateLateFeeAmount(Long loanId);
} 