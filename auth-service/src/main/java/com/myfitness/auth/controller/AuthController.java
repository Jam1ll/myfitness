package com.myfitness.auth.controller;

import com.myfitness.auth.dto.AuthResponse;
import com.myfitness.auth.dto.GoogleLoginRequest;
import com.myfitness.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for Google OAuth2 login, automatic user registration, and JWT token generation")
public class AuthController {

    private final AuthService authService;

    // GOOGLE LOGIN / REGISTER -> POST /api/v1/auth/google
    @Operation(
            summary = "Authenticate or register user with Google ID Token",
            description = "Verifies the Google OAuth2 ID Token signature, registers the user in the database if it is their first login, and returns a signed internal JWT token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully and JWT issued"),
            @ApiResponse(responseCode = "400", description = "Invalid or malformed request body"),
            @ApiResponse(responseCode = "500", description = "Invalid Google signature, expired token, or internal authentication error")
    })
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.authenticateWithGoogle(request.getIdToken());
        return ResponseEntity.ok(response);
    }
}