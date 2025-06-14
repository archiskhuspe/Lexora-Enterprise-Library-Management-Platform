package com.library.management.controller;

import com.library.management.dto.LoanDTO;
import com.library.management.dto.request.LoanSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import com.library.management.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan Management", description = "APIs for managing book loans")
public class LoanController extends BaseController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/borrow")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Borrow a book", description = "Create a new loan for a book")
    @ApiResponse(responseCode = "200", description = "Book borrowed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Book or member not found")
    @ApiResponse(responseCode = "409", description = "Book not available or member has reached loan limit")
    public ResponseEntity<LoanDTO> borrowBook(@Valid @RequestBody LoanDTO loanDTO) {
        return ok(loanService.borrowBook(loanDTO));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Return a book", description = "Process a book return")
    @ApiResponse(responseCode = "200", description = "Book returned successfully")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    @ApiResponse(responseCode = "409", description = "Book already returned")
    public ResponseEntity<LoanDTO> returnBook(@PathVariable Long id) {
        return ok(loanService.returnBook(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'MEMBER')")
    @Operation(summary = "Get loan details", description = "Retrieve details of a specific loan")
    @ApiResponse(responseCode = "200", description = "Loan details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public ResponseEntity<LoanDTO> getLoan(@PathVariable Long id) {
        return ok(loanService.getLoanById(id));
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'MEMBER')")
    @Operation(summary = "Get member's loans", description = "Retrieve all loans for a specific member")
    @ApiResponse(responseCode = "200", description = "Member's loans retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Member not found")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<LoanDTO>> getMemberLoans(
            @PathVariable Long memberId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return ok(loanService.getLoansByMember(memberId, pageNo, pageSize));
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN')")
    @Operation(summary = "Get book's loans", description = "Retrieve all loans for a specific book")
    @ApiResponse(responseCode = "200", description = "Book's loans retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Book not found")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<LoanDTO>> getBookLoans(
            @PathVariable Long bookId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return ok(loanService.getLoansByBook(bookId, pageNo, pageSize));
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get all loans", description = "Retrieve all loans with pagination")
    @ApiResponse(responseCode = "200", description = "Loans retrieved successfully")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<LoanDTO>> getAllLoans(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return ok(loanService.getAllLoans(pageNo, pageSize));
    }

    @PostMapping("/list")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get all loans with filters", description = "Retrieve all loans with filters and pagination")
    @ApiResponse(responseCode = "200", description = "Loans retrieved successfully")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<LoanDTO>> getLoanListWithFilters(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody(required = false) LoanSearchRequest searchRequest) {
        return ok(loanService.getLoanListWithFilters(pageNo, pageSize, searchRequest));
    }
} 