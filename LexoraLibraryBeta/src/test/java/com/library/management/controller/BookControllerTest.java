package com.library.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.dto.BookDTO;
import com.library.management.dto.request.BookSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import com.library.management.service.BookService;
import com.library.management.config.TestSecurityConfig;
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
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookDTO testBookDTO;

    @BeforeEach
    void setUp() {
        testBookDTO = BookDTO.builder()
            .id(1L)
            .title("Test Book")
            .author("Test Author")
            .isbn("1234567890")
            .category("Test Category")
            .publicationYear(2024)
            .totalCopies(5)
            .availableCopies(5)
            .build();
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void createBook_Success() throws Exception {
        when(bookService.createBook(any(BookDTO.class))).thenReturn(testBookDTO);

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBookDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testBookDTO.getId()))
                .andExpect(jsonPath("$.title").value(testBookDTO.getTitle()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void createBook_ValidationFailure() throws Exception {
        BookDTO invalidBook = BookDTO.builder().build();

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void updateBook_Success() throws Exception {
        when(bookService.updateBook(anyLong(), any(BookDTO.class))).thenReturn(testBookDTO);

        mockMvc.perform(put("/api/books/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBookDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBookDTO.getId()))
                .andExpect(jsonPath("$.title").value(testBookDTO.getTitle()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void deleteBook_Success() throws Exception {
        doNothing().when(bookService).deleteBook(anyLong());

        mockMvc.perform(delete("/api/books/{id}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "MEMBER"})
    void getBookById_Success() throws Exception {
        when(bookService.getBookById(anyLong())).thenReturn(testBookDTO);

        mockMvc.perform(get("/api/books/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBookDTO.getId()))
                .andExpect(jsonPath("$.title").value(testBookDTO.getTitle()));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "MEMBER"})
    void getAllBooks_Success() throws Exception {
        Page<BookDTO> page = new PageImpl<>(Arrays.asList(testBookDTO));
        when(bookService.getBookListWithFilters(any(), any(), any())).thenReturn(PaginatedResponse.from(page));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testBookDTO.getId()))
                .andExpect(jsonPath("$.content[0].title").value(testBookDTO.getTitle()));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "MEMBER"})
    void searchBooks_Success() throws Exception {
        Page<BookDTO> page = new PageImpl<>(Arrays.asList(testBookDTO));
        when(bookService.getBookListWithFilters(any(), any(), any())).thenReturn(PaginatedResponse.from(page));

        mockMvc.perform(get("/api/books/search")
                .param("title", "Test")
                .param("author", "Author")
                .param("category", "Category")
                .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testBookDTO.getId()))
                .andExpect(jsonPath("$.content[0].title").value(testBookDTO.getTitle()));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "MEMBER"})
    void searchBooks_NoParams_Success() throws Exception {
        Page<BookDTO> page = new PageImpl<>(Arrays.asList(testBookDTO));
        when(bookService.getBookListWithFilters(any(), any(), any())).thenReturn(PaginatedResponse.from(page));

        mockMvc.perform(get("/api/books/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testBookDTO.getId()))
                .andExpect(jsonPath("$.content[0].title").value(testBookDTO.getTitle()));
    }
} 