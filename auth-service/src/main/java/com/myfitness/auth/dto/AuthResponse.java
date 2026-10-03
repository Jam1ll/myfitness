package com.myfitness.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {

    private UUID id;
    private String name;
    private String username;
    private String email;
    private String token;
    private List<String> roles;
    private boolean isVerified;
}
