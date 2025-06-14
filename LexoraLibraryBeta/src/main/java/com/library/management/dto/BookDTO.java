package com.library.management.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "ISBN is required")
    @Pattern(
        regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
        message = "Invalid ISBN format"
    )
    private String isbn;

    @NotBlank(message = "Category is required")
    private String category;

    @Min(value = 1800, message = "Publication year must be after 1800")
    @Max(value = 2100, message = "Publication year must be before 2100")
    private Integer publicationYear;

    @Min(value = 0, message = "Total copies must be non-negative")
    private Integer totalCopies;

    private Integer availableCopies;
} 