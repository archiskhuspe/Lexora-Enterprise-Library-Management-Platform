package com.library.management.service;

import com.library.management.dto.BookDTO;
import com.library.management.dto.MemberDTO;
import com.library.management.entity.Book;
import com.library.management.entity.Member;
import com.library.management.repository.BookRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    private Book testBook;
    private Member testMember;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setCategory("Test Category");
        testBook.setIsbn("1234567890");

        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("Test Member");
        testMember.setEmail("test@example.com");
        testMember.setMembershipId("MEM-12345678");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void searchBooks_Success() {
        Page<Book> bookPage = new PageImpl<>(List.of(testBook));
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(bookPage);

        Page<BookDTO> result = searchService.searchBooks("test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBook.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    void searchBooksByTitle_Success() {
        Page<Book> bookPage = new PageImpl<>(List.of(testBook));
        when(bookRepository.findByTitleContainingIgnoreCase(any(String.class), any(Pageable.class)))
                .thenReturn(bookPage);

        Page<BookDTO> result = searchService.searchBooksByTitle("Test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBook.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    void searchMembers_Success() {
        Page<Member> memberPage = new PageImpl<>(List.of(testMember));
        when(memberRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(memberPage);

        Page<MemberDTO> result = searchService.searchMembers("test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testMember.getName(), result.getContent().get(0).getName());
    }

    @Test
    void searchMembersByName_Success() {
        Page<Member> memberPage = new PageImpl<>(List.of(testMember));
        when(memberRepository.findByNameContainingIgnoreCase(any(String.class), any(Pageable.class)))
                .thenReturn(memberPage);

        Page<MemberDTO> result = searchService.searchMembersByName("Test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testMember.getName(), result.getContent().get(0).getName());
    }
} 