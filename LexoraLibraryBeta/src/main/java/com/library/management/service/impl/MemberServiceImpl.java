package com.library.management.service.impl;

import com.library.management.dto.MemberDTO;
import com.library.management.dto.request.MemberSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import com.library.management.entity.Member;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.MemberRepository;
import com.library.management.service.MemberService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberDTO createMember(MemberDTO memberDTO) {
        if (memberRepository.existsByEmail(memberDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Member member = new Member();
        BeanUtils.copyProperties(memberDTO, member);
        member = memberRepository.save(member);
        
        MemberDTO savedMemberDTO = new MemberDTO();
        BeanUtils.copyProperties(member, savedMemberDTO);
        return savedMemberDTO;
    }

    @Override
    public MemberDTO getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        
        MemberDTO memberDTO = new MemberDTO();
        BeanUtils.copyProperties(member, memberDTO);
        return memberDTO;
    }

    @Override
    public MemberDTO getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with email: " + email));
        
        MemberDTO memberDTO = new MemberDTO();
        BeanUtils.copyProperties(member, memberDTO);
        return memberDTO;
    }

    @Override
    public MemberDTO getMemberByMembershipId(String membershipId) {
        Member member = memberRepository.findByMembershipId(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with membership ID: " + membershipId));
        
        MemberDTO memberDTO = new MemberDTO();
        BeanUtils.copyProperties(member, memberDTO);
        return memberDTO;
    }

    @Override
    public MemberDTO updateMember(Long id, MemberDTO memberDTO) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        if (!member.getEmail().equals(memberDTO.getEmail()) && 
            memberRepository.existsByEmail(memberDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        BeanUtils.copyProperties(memberDTO, member, "id", "membershipId", "active", "createdAt");
        member = memberRepository.save(member);
        
        MemberDTO updatedMemberDTO = new MemberDTO();
        BeanUtils.copyProperties(member, updatedMemberDTO);
        return updatedMemberDTO;
    }

    @Override
    public void deactivateMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        member.setActive(false);
        memberRepository.save(member);
    }

    @Override
    public boolean canBorrow(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return member.isActive();
    }

    @Override
    public Page<MemberDTO> searchMembers(String name, String email, String membershipId, 
                                       String phoneNumber, Boolean active, Pageable pageable) {
        return memberRepository.searchMembers(name, email, membershipId, phoneNumber, active, pageable)
                .map(member -> {
                    MemberDTO dto = new MemberDTO();
                    BeanUtils.copyProperties(member, dto);
                    return dto;
                });
    }

    @Override
    public Page<MemberDTO> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(member -> {
                    MemberDTO dto = new MemberDTO();
                    BeanUtils.copyProperties(member, dto);
                    return dto;
                });
    }

    @Override
    public PaginatedResponse<MemberDTO> getMemberListWithFilters(Integer pageNo, Integer pageSize, MemberSearchRequest searchRequest) {
        Page<Member> membersPage = memberRepository.searchMembers(
            searchRequest.getName(),
            searchRequest.getEmail(),
            searchRequest.getMembershipId(),
            searchRequest.getPhoneNumber(),
            searchRequest.getActive(),
            PageRequest.of(pageNo - 1, pageSize)
        );

        return PaginatedResponse.from(membersPage.map(member -> {
            MemberDTO dto = new MemberDTO();
            BeanUtils.copyProperties(member, dto);
            return dto;
        }));
    }
} 