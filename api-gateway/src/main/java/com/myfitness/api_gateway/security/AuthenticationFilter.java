package com.myfitness.api_gateway.security;

import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouteValidator validator, JwtUtil jwtUtil) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(@NonNull Config config) {
        return ((exchange, chain) -> {

            // 1. Verificamos si la ruta actual exige seguridad
            if (validator.isSecured.test(exchange.getRequest())) {

                // 2. Si exige seguridad, verificamos que traiga el Header 'Authorization'
                if (!exchange.getRequest().getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Auth header missing");
                }

                // 3. Extraemos el Header
                String authHeader = Objects.requireNonNull
                        (exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION)).getFirst();

                if (authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7); // Quitamos la palabra "Bearer "
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid string format");
                }

                // 4. Intentamos validar la firma matemática del token
                try {
                    jwtUtil.validateToken(authHeader);

                    String userEmail = jwtUtil.extractEmail(authHeader);
                    ServerHttpRequest modifiedRequest = exchange.getRequest()
                            .mutate()
                            .header("X-User-Email", userEmail)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    System.out.println("Invalid or expired token: " + e.getMessage());
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access not authorized");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}