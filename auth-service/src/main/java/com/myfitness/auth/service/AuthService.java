package com.myfitness.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.myfitness.auth.dto.AuthResponse;
import com.myfitness.auth.model.User;
import com.myfitness.auth.repository.AuthRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @Value("${jwt.key}")
    private String jwtKey;
    @Value("${jwt.duration-in-minutes}")
    private int jwtDurationInMinutes;
    @Value("${jwt.issuer}")
    private String jwtIssuer;
    @Value("${jwt.audience}")
    private String jwtAudience;

    @Transactional
    public AuthResponse authenticateWithGoogle(String idToken) {
        try {
            // 1. Validate Google token using injected verifier
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken == null) {
                throw new RuntimeException("invalid google signature or expired token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("given_name");

            // 2. Find existing user or register a new one
            User user = authRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .username(email)
                        .name(name != null ? name : "not_found_name")
                        .emailConfirmed(true)
                        .roles(List.of("BASIC"))
                        .build();
                return authRepository.save(newUser);
            });

            // 3. Generate JWT token
            String token = generateJWTToken(user);

            // 4. Return DTO
            return AuthResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .token(token)
                    .roles(user.getRoles())
                    .isVerified(user.isEmailConfirmed())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("google auth error: " + e.getMessage(), e);
        }
    }

    private String generateJWTToken(User user) {
        Key key = Keys.hmacShaKeyFor(jwtKey.getBytes());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (jwtDurationInMinutes * 60000L));

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setId(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("uid", user.getId())
                .claim("roles", user.getRoles())
                .claim("name", user.getName())
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}