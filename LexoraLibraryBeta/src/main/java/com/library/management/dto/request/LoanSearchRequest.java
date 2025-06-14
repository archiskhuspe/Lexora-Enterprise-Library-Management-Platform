package com.library.management.dto.request;

import com.library.management.entity.Loan;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoanSearchRequest {
    private Long memberId;
    private Long bookId;
    private Loan.LoanStatus status;
    private LocalDateTime borrowDateFrom;
    private LocalDateTime borrowDateTo;
    private LocalDateTime returnDateFrom;
    private LocalDateTime returnDateTo;
    private Boolean overdue;
} 