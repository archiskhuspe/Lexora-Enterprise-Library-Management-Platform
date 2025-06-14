package com.library.management.dto.request;

import lombok.Data;

@Data
public class BookSearchRequest {
    private String title;
    private String author;
    private String isbn;
    private String category;
    private Integer publicationYear;
    private Boolean available;
} 