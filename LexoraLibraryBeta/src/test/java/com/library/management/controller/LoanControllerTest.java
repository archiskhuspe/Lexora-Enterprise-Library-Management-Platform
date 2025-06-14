package com.library.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.dto.LoanDTO;
import com.library.management.dto.request.LoanSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import com.library.management.service.LoanService;
import com.library.management.config.TestSecurityConfig;
import com.library.management.entity.Loan.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    private LoanDTO testLoanDTO;

    @BeforeEach
    void setUp() {
        testLoanDTO = LoanDTO.builder()
            .id(1L)
            .bookId(1L)
            .memberId(1L)
            .borrowDate(LocalDateTime.now())
            .expectedReturnDate(LocalDateTime.now().plusDays(14))
            .status(LoanStatus.ACTIVE)
            .build();
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "MEMBER"})
    void getLoan_Success() throws Exception {
        when(loanService.getLoanById(anyLong())).thenReturn(testLoanDTO);

        mockMvc.perform(get("/api/loans/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLoanDTO.getId()))
                .andExpect(jsonPath("$.bookId").value(testLoanDTO.getBookId()));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "MEMBER"})
    void getBookLoans_Success() throws Exception {
        PaginatedResponse<LoanDTO> response = PaginatedResponse.from(new PageImpl<>(Arrays.asList(testLoanDTO)));
        when(loanService.getLoansByBook(anyLong(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/loans/book/{bookId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testLoanDTO.getId()))
                .andExpect(jsonPath("$.content[0].bookId").value(testLoanDTO.getBookId()));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "MEMBER"})
    void getMemberLoans_Success() throws Exception {
        PaginatedResponse<LoanDTO> response = PaginatedResponse.from(new PageImpl<>(Arrays.asList(testLoanDTO)));
        when(loanService.getLoansByMember(anyLong(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/loans/member/{memberId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testLoanDTO.getId()))
                .andExpect(jsonPath("$.content[0].memberId").value(testLoanDTO.getMemberId()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void borrowBook_Success() throws Exception {
        when(loanService.borrowBook(any(LoanDTO.class))).thenReturn(testLoanDTO);

        mockMvc.perform(post("/api/loans/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\": 1, \"memberId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLoanDTO.getId()))
                .andExpect(jsonPath("$.bookId").value(testLoanDTO.getBookId()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void returnBook_Success() throws Exception {
        when(loanService.returnBook(anyLong())).thenReturn(testLoanDTO);

        mockMvc.perform(post("/api/loans/{id}/return", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLoanDTO.getId()))
                .andExpect(jsonPath("$.status").value(testLoanDTO.getStatus().toString()));
    }
} 