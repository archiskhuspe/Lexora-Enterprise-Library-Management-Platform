package com.library.management.dto;

import com.library.management.entity.LateFee;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LateFeeDTO {
    private Long id;
    private Long loanId;
    private BigDecimal amount;
    private Integer daysOverdue;
    private LocalDateTime paidDate;
    private LateFee.FeeStatus status;
    private String memberName;
    private String bookTitle;

    // Default constructor
    public LateFeeDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Integer daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public LateFee.FeeStatus getStatus() {
        return status;
    }

    public void setStatus(LateFee.FeeStatus status) {
        this.status = status;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
} 