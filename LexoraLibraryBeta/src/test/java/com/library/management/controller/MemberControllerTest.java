package com.library.management.controller;

import com.library.management.dto.MemberDTO;
import com.library.management.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private MemberDTO testMemberDTO;

    @BeforeEach
    void setUp() {
        testMemberDTO = new MemberDTO();
        testMemberDTO.setId(1L);
        testMemberDTO.setName("John Doe");
        testMemberDTO.setEmail("john.doe@example.com");
        testMemberDTO.setPhoneNumber("+1234567890");
        testMemberDTO.setActive(true);
    }

    @Test
    void testSearchMembers() {
        // Given
        Page<MemberDTO> page = new PageImpl<>(Collections.singletonList(testMemberDTO));
        when(memberService.searchMembers(any(), any(), any(), any(), any(), any())).thenReturn(page);

        // When
        ResponseEntity<Page<MemberDTO>> response = memberController.searchMembers(
            "John", "john.doe@example.com", null, null, true, 0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(testMemberDTO.getId(), response.getBody().getContent().get(0).getId());
        assertEquals(testMemberDTO.getName(), response.getBody().getContent().get(0).getName());
    }

    @Test
    void testCreateMember() {
        // Given
        MemberDTO inputDTO = new MemberDTO();
        inputDTO.setName("John Doe");
        inputDTO.setEmail("john@example.com");
        inputDTO.setPhoneNumber("+1234567890");

        MemberDTO savedDTO = new MemberDTO();
        savedDTO.setId(1L);
        savedDTO.setName("John Doe");
        savedDTO.setEmail("john@example.com");
        savedDTO.setPhoneNumber("+1234567890");
        savedDTO.setActive(true);

        when(memberService.createMember(any(MemberDTO.class))).thenReturn(savedDTO);

        // When
        ResponseEntity<MemberDTO> response = memberController.createMember(inputDTO);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());
        verify(memberService).createMember(any(MemberDTO.class));
    }

    @Test
    void testGetMemberById() {
        // Given
        Long memberId = 1L;
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(memberId);
        memberDTO.setName("John Doe");
        memberDTO.setEmail("john@example.com");

        when(memberService.getMemberById(memberId)).thenReturn(memberDTO);

        // When
        ResponseEntity<MemberDTO> response = memberController.getMemberById(memberId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(memberId, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    void testCanBorrow() {
        // Given
        Long memberId = 1L;
        when(memberService.canBorrow(memberId)).thenReturn(true);

        // When
        ResponseEntity<Boolean> response = memberController.canBorrow(memberId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }
} 