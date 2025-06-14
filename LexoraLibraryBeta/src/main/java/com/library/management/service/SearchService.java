package com.library.management.service;

import com.library.management.dto.BookDTO;
import com.library.management.dto.MemberDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    Page<BookDTO> searchBooks(String query, Pageable pageable);
    Page<BookDTO> searchBooksByTitle(String title, Pageable pageable);
    Page<BookDTO> searchBooksByAuthor(String author, Pageable pageable);
    Page<BookDTO> searchBooksByCategory(String category, Pageable pageable);
    Page<BookDTO> searchBooksByAvailability(boolean available, Pageable pageable);

    Page<MemberDTO> searchMembers(String query, Pageable pageable);
    Page<MemberDTO> searchMembersByName(String name, Pageable pageable);
    Page<MemberDTO> searchMembersByEmail(String email, Pageable pageable);
    Page<MemberDTO> searchMembersByMembershipId(String membershipId, Pageable pageable);
    Page<MemberDTO> searchMembersByStatus(boolean active, Pageable pageable);
} 