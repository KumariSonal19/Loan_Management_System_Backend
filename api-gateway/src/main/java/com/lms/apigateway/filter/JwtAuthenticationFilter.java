package com.lms.apigateway.filter;

import com.lms.apigateway.util.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            HttpMethod method = exchange.getRequest().getMethod();

            log.info("Processing Request: {} {}", method, path);

            if (isPublicRoute(path)) {
                log.info("Public route detected. Allowing access.");
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return this.unauthorized(exchange);
            }

            String token = authHeader.substring(7);

            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Invalid JWT Token");
                return this.unauthorized(exchange);
            }

            String role = jwtTokenProvider.getRoleFromToken(token);
            log.info("User Role: {}", role);

//            if (isAdminRoute(path)) {
//                if (!"ROLE_ADMIN".equals(role)) {
//                    log.warn("Access Denied: Admin role required");
//                    return this.forbidden(exchange);
//                }
//            }

            if (isLoanOfficerRoute(path, method)) {
                if (!"ROLE_ADMIN".equals(role) && !"ROLE_LOAN_OFFICER".equals(role)) {
                    log.warn("Access Denied: Loan Officer role required");
                    return this.forbidden(exchange);
                }
            }

            return chain.filter(exchange);
        };
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/api/auth")  
            || path.contains("/health")         
            || path.startsWith("/actuator")
            || path.startsWith("/v3/api-docs");
    }

//    private boolean isAdminRoute(String path) {
//        return path.startsWith("/api/admin"); 
//    }

    private boolean isLoanOfficerRoute(String path, HttpMethod method) {
        return path.contains("/loans/review") && HttpMethod.PUT.equals(method);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    public static class Config {}
}