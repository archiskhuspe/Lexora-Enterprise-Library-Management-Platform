package com.library.management.dto.request;

import lombok.Data;

@Data
public class MemberSearchRequest {
    private String name;
    private String email;
    private String membershipId;
    private String phoneNumber;
    private Boolean active;
} 