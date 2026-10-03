package com.myfitness.catalog_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MyFitness - Catalog Microservice API",
                version = "1.0.0",
                description = "REST API for managing the global exercise catalog, muscle group filtering, and batch validation.",
                contact = @Contact(
                        name = "Jamil Guzman",
                        email = "jamilguzman202@gmail.com"
                )
        ),
        // Forces Swagger UI to send all requests through the API Gateway (port 8080)
        servers = {
                @Server(url = "http://localhost:8080", description = "API Gateway (Secured with JWT)")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Paste your JWT token obtained from /api/v1/auth/google",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}