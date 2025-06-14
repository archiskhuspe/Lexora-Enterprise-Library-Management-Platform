package com.library.management.controller;

import com.library.management.dto.MemberDTO;
import com.library.management.dto.request.MemberSearchRequest;
import com.library.management.dto.response.PaginatedResponse;
import com.library.management.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member Management", description = "APIs for managing library members")
@PreAuthorize("hasRole('LIBRARIAN')")
public class MemberController extends BaseController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @Operation(summary = "Create a new member")
    public ResponseEntity<MemberDTO> createMember(@Valid @RequestBody MemberDTO memberDTO) {
        return new ResponseEntity<>(memberService.createMember(memberDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID")
    public ResponseEntity<MemberDTO> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get member by email")
    public ResponseEntity<MemberDTO> getMemberByEmail(@PathVariable String email) {
        return ResponseEntity.ok(memberService.getMemberByEmail(email));
    }

    @GetMapping("/membership/{membershipId}")
    @Operation(summary = "Get member by membership ID")
    public ResponseEntity<MemberDTO> getMemberByMembershipId(@PathVariable String membershipId) {
        return ResponseEntity.ok(memberService.getMemberByMembershipId(membershipId));
    }

    @GetMapping
    @Operation(summary = "Get all members with pagination")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<MemberDTO>> getAllMembers(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return ok(memberService.getMemberListWithFilters(pageNo, pageSize, null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search members with filters")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<MemberDTO>> searchMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String membershipId,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(memberService.searchMembers(
            name, email, membershipId, phoneNumber, active, 
            PageRequest.of(pageNo, pageSize)));
    }

    @Transactional(readOnly = true)
    @PostMapping("/list")
    @Operation(summary = "Get all members with filters and pagination")
    public ResponseEntity<PaginatedResponse<MemberDTO>> getMemberListWithFilters(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody(required = false) MemberSearchRequest searchRequest) {
        return ok(memberService.getMemberListWithFilters(pageNo, pageSize, searchRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update member details")
    public ResponseEntity<MemberDTO> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberDTO memberDTO) {
        return ResponseEntity.ok(memberService.updateMember(id, memberDTO));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a member")
    public ResponseEntity<Void> deactivateMember(@PathVariable Long id) {
        memberService.deactivateMember(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/can-borrow")
    @Operation(summary = "Check if member can borrow more books")
    public ResponseEntity<Boolean> canBorrow(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.canBorrow(id));
    }
} 