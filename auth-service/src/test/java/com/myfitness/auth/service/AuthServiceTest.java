package com.myfitness.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.myfitness.auth.dto.AuthResponse;
import com.myfitness.auth.model.User;
import com.myfitness.auth.repository.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // External dependencies Mocks
    @Mock
    private AuthRepository authRepository;

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Mock
    private GoogleIdToken googleIdToken;

    @Mock
    private GoogleIdToken.Payload payload;

    // Inject mocks into the real service
    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Inject @Value properties for JWT generation (must be at least 32 bytes for HS256)
        ReflectionTestUtils.setField(authService, "jwtKey", "mySuperSecretTestKeyThatIsAtLeast32BytesLong123456");
        ReflectionTestUtils.setField(authService, "jwtDurationInMinutes", 60);
        ReflectionTestUtils.setField(authService, "jwtIssuer", "myfitness-auth-test");
        ReflectionTestUtils.setField(authService, "jwtAudience", "myfitness-client-test");
    }

    //
    // 1. AUTHENTICATE WITH GOOGLE - EXISTING USER (HAPPY PATH)
    //
    @Test
    @DisplayName("Should authenticate existing user and return JWT token without saving a new user")
    void authenticateWithGoogle_WhenUserAlreadyExists_ShouldReturnAuthResponse() throws Exception {
        //
        // GIVEN
        //
        String fakeGoogleToken = "valid.google.token";
        String userEmail = "existing@gmail.com";
        UUID existingUserId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(existingUserId)
                .email(userEmail)
                .username(userEmail)
                .name("Jamil")
                .emailConfirmed(true)
                .roles(List.of("BASIC"))
                .build();

        // 1. when Google verifier checks the token, return mocked GoogleIdToken and Payload
        when(googleIdTokenVerifier.verify(fakeGoogleToken)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn(userEmail);
        when(payload.get("given_name")).thenReturn("Jamil");

        // 2. when repository searches by email, return the existing user
        when(authRepository.findByEmail(userEmail)).thenReturn(Optional.of(existingUser));

        //
        // WHEN
        //
        AuthResponse actualResponse = authService.authenticateWithGoogle(fakeGoogleToken);

        //
        // THEN
        //
        assertNotNull(actualResponse);
        assertEquals(existingUserId, actualResponse.getId());
        assertEquals(userEmail, actualResponse.getEmail());
        assertNotNull(actualResponse.getToken());
        assertFalse(actualResponse.getToken().isBlank());

        // verify repository searched by email once and never saved a duplicate user
        verify(authRepository, times(1)).findByEmail(userEmail);
        verify(authRepository, never()).save(any(User.class));
    }

    //
    // 2. AUTHENTICATE WITH GOOGLE - NEW USER REGISTRATION
    //
    @Test
    @DisplayName("Should register and save a new user in DB when email does not exist yet")
    void authenticateWithGoogle_WhenNewUser_ShouldSaveUserAndReturnAuthResponse() throws Exception {
        //
        // GIVEN
        //
        String fakeGoogleToken = "valid.google.token";
        String newEmail = "newuser@gmail.com";
        UUID generatedUserId = UUID.randomUUID();

        User savedUser = User.builder()
                .id(generatedUserId)
                .email(newEmail)
                .username(newEmail)
                .name("Sofia")
                .emailConfirmed(true)
                .roles(List.of("BASIC"))
                .build();

        // 1. when Google verifier checks the token, return mocked GoogleIdToken and Payload
        when(googleIdTokenVerifier.verify(fakeGoogleToken)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn(newEmail);
        when(payload.get("given_name")).thenReturn("Sofia");

        // 2. when repository searches by email, return empty Optional (user does not exist)
        when(authRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        // 3. when repository saves the new user, return savedUser
        when(authRepository.save(any(User.class))).thenReturn(savedUser);

        //
        // WHEN
        //
        AuthResponse actualResponse = authService.authenticateWithGoogle(fakeGoogleToken);

        //
        // THEN
        //
        assertNotNull(actualResponse);
        assertEquals(generatedUserId, actualResponse.getId());
        assertEquals(newEmail, actualResponse.getEmail());
        assertEquals("Sofia", actualResponse.getName());
        assertNotNull(actualResponse.getToken());

        // verify repository searched once and saved the new user once
        verify(authRepository, times(1)).findByEmail(newEmail);
        verify(authRepository, times(1)).save(any(User.class));
    }

    //
    // 3. AUTHENTICATE WITH GOOGLE - INVALID OR EXPIRED TOKEN
    //
    @Test
    @DisplayName("Should throw RuntimeException and never access DB when Google token is invalid or expired")
    void authenticateWithGoogle_WhenTokenIsInvalid_ShouldThrowRuntimeException() throws Exception {
        //
        // GIVEN
        //
        String invalidGoogleToken = "expired.or.tampered.token";

        // 1. when Google verifier checks an invalid token, it returns null
        when(googleIdTokenVerifier.verify(invalidGoogleToken)).thenReturn(null);

        //
        // WHEN & THEN
        //
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.authenticateWithGoogle(invalidGoogleToken)
        );

        assertTrue(exception.getMessage().contains("invalid google"));

        // database must never be queried if Google token verification fails
        verify(authRepository, never()).findByEmail(any());
        verify(authRepository, never()).save(any());
    }
}