package com.library.management.service.impl;

import com.library.management.dto.BookDTO;
import com.library.management.dto.MemberDTO;
import com.library.management.entity.Book;
import com.library.management.entity.Member;
import com.library.management.repository.BookRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.service.SearchService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public SearchServiceImpl(BookRepository bookRepository, MemberRepository memberRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public Page<BookDTO> searchBooks(String query, Pageable pageable) {
        return bookRepository.findAll(createBookSpecification(query), pageable).map(this::convertToBookDTO);
    }

    @Override
    public Page<BookDTO> searchBooksByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::convertToBookDTO);
    }

    @Override
    public Page<BookDTO> searchBooksByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable)
                .map(this::convertToBookDTO);
    }

    @Override
    public Page<BookDTO> searchBooksByCategory(String category, Pageable pageable) {
        return bookRepository.findByCategoryContainingIgnoreCase(category, pageable)
                .map(this::convertToBookDTO);
    }

    @Override
    public Page<BookDTO> searchBooksByAvailability(boolean available, Pageable pageable) {
        return bookRepository.findByAvailableCopiesGreaterThan(available ? 0 : -1, pageable)
                .map(this::convertToBookDTO);
    }

    @Override
    public Page<MemberDTO> searchMembers(String query, Pageable pageable) {
        Specification<Member> spec = (root, criteriaQuery, criteriaBuilder) -> {
            String likePattern = "%" + query.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("membershipId")), likePattern)
            );
        };
        return memberRepository.findAll(spec, pageable).map(this::convertToMemberDTO);
    }

    @Override
    public Page<MemberDTO> searchMembersByName(String name, Pageable pageable) {
        return memberRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::convertToMemberDTO);
    }

    @Override
    public Page<MemberDTO> searchMembersByEmail(String email, Pageable pageable) {
        return memberRepository.findByEmailContainingIgnoreCase(email, pageable)
                .map(this::convertToMemberDTO);
    }

    @Override
    public Page<MemberDTO> searchMembersByMembershipId(String membershipId, Pageable pageable) {
        return memberRepository.findByMembershipIdContainingIgnoreCase(membershipId, pageable)
                .map(this::convertToMemberDTO);
    }

    @Override
    public Page<MemberDTO> searchMembersByStatus(boolean active, Pageable pageable) {
        return memberRepository.findByActive(active, pageable)
                .map(this::convertToMemberDTO);
    }

    private Specification<Book> createBookSpecification(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String searchTerm = "%" + query.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("isbn")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), searchTerm)
                ));
            }

            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private BookDTO convertToBookDTO(Book book) {
        BookDTO dto = new BookDTO();
        BeanUtils.copyProperties(book, dto);
        return dto;
    }

    private MemberDTO convertToMemberDTO(Member member) {
        MemberDTO dto = new MemberDTO();
        BeanUtils.copyProperties(member, dto);
        return dto;
    }
} 