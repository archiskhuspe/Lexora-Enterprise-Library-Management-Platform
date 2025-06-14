package com.library.management.service;

import com.library.management.dto.MemberDTO;
import com.library.management.entity.Member;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.MemberRepository;
import com.library.management.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member testMember;
    private MemberDTO testMemberDTO;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("John Doe");
        testMember.setEmail("john@example.com");
        testMember.setPhoneNumber("+1234567890");
        testMember.setMembershipId("MEM-12345678");
        testMember.setActive(true);
        testMember.setCreatedAt(LocalDateTime.now());
        testMember.setUpdatedAt(LocalDateTime.now());

        testMemberDTO = new MemberDTO();
        testMemberDTO.setName("John Doe");
        testMemberDTO.setEmail("john@example.com");
        testMemberDTO.setPhoneNumber("+1234567890");
    }

    @Test
    void createMember_Success() {
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(memberRepository.countActiveLoans(anyLong())).thenReturn(0);

        MemberDTO result = memberService.createMember(testMemberDTO);

        assertNotNull(result);
        assertEquals(testMemberDTO.getName(), result.getName());
        assertEquals(testMemberDTO.getEmail(), result.getEmail());
        assertTrue(result.getMembershipId().startsWith("MEM-"));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void createMember_DuplicateEmail() {
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
            memberService.createMember(testMemberDTO)
        );
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void getMemberById_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.countActiveLoans(anyLong())).thenReturn(0);

        MemberDTO result = memberService.getMemberById(1L);

        assertNotNull(result);
        assertEquals(testMember.getName(), result.getName());
        assertEquals(testMember.getEmail(), result.getEmail());
    }

    @Test
    void getMemberById_NotFound() {
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            memberService.getMemberById(1L)
        );
    }

    @Test
    void getAllMembers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> memberPage = new PageImpl<>(List.of(testMember));
        when(memberRepository.findAll(pageable)).thenReturn(memberPage);
        when(memberRepository.countActiveLoans(anyLong())).thenReturn(0);

        Page<MemberDTO> result = memberService.getAllMembers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testMember.getName(), result.getContent().get(0).getName());
    }

    @Test
    void updateMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(memberRepository.countActiveLoans(anyLong())).thenReturn(0);

        testMemberDTO.setName("Updated Name");
        MemberDTO result = memberService.updateMember(1L, testMemberDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void deactivateMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        memberService.deactivateMember(1L);

        verify(memberRepository).save(any(Member.class));
        assertFalse(testMember.isActive());
    }

    @Test
    void canBorrow_UnderLimit() {
        when(memberRepository.countActiveLoans(1L)).thenReturn(3);

        boolean result = memberService.canBorrow(1L);

        assertTrue(result);
    }

    @Test
    void canBorrow_AtLimit() {
        when(memberRepository.countActiveLoans(1L)).thenReturn(5);

        boolean result = memberService.canBorrow(1L);

        assertFalse(result);
    }
} 