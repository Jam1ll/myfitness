package com.myfitness.auth.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MyFitness - Auth Microservice API",
                version = "1.0.0",
                description = "REST API for OAuth2 Google authentication, user registration, and JWT token issuance.",
                contact = @Contact(
                        name = "Jamil Guzman",
                        email = "jamilguzman202@gmail.com"
                )
        )
)
public class GoogleAuthConfig {

    @Value("${google.client-id}")
    private String googleClientId;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .setAcceptableTimeSkewSeconds(300)
                .build();
    }
}