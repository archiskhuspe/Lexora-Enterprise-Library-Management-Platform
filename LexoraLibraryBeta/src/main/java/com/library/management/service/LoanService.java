package com.library.management.service;

import com.library.management.dto.LoanDTO;
import com.library.management.dto.request.LoanSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanService {
    LoanDTO borrowBook(LoanDTO loanDTO);
    LoanDTO returnBook(Long id);
    LoanDTO getLoanById(Long id);
    Page<LoanDTO> getLoansByMember(Long memberId, Pageable pageable);
    Page<LoanDTO> getLoansByBook(Long bookId, Pageable pageable);
    PaginatedResponse<LoanDTO> getLoansByMember(Long memberId, Integer pageNo, Integer pageSize);
    PaginatedResponse<LoanDTO> getLoansByBook(Long bookId, Integer pageNo, Integer pageSize);
    PaginatedResponse<LoanDTO> getAllLoans(Integer pageNo, Integer pageSize);
    PaginatedResponse<LoanDTO> getLoanListWithFilters(Integer pageNo, Integer pageSize, LoanSearchRequest searchRequest);
    void updateOverdueLoans();
    boolean canMemberBorrow(Long memberId);
    long getActiveLoansCount(Long memberId);
} 