package com.library.management.service;

import com.library.management.dto.MemberDTO;
import com.library.management.dto.request.MemberSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
    MemberDTO createMember(MemberDTO memberDTO);
    MemberDTO getMemberById(Long id);
    MemberDTO getMemberByEmail(String email);
    MemberDTO getMemberByMembershipId(String membershipId);
    MemberDTO updateMember(Long id, MemberDTO memberDTO);
    void deactivateMember(Long id);
    boolean canBorrow(Long id);
    Page<MemberDTO> searchMembers(String name, String email, String membershipId, 
                                String phoneNumber, Boolean active, Pageable pageable);
    Page<MemberDTO> getAllMembers(Pageable pageable);
    PaginatedResponse<MemberDTO> getMemberListWithFilters(Integer pageNo, Integer pageSize, MemberSearchRequest searchRequest);
} 