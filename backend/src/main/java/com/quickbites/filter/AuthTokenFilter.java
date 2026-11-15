package com.quickbites.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbites.dto.ErrorResponse;
import com.quickbites.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/**
 * Resolves the "Authorization: Bearer <token>" header to a user id and exposes it
 * as the request attribute {@link #USER_ID_ATTR}. Public endpoints are let through.
 */
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTR = "quickbites.userId";

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthTokenFilter(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) return true;
        if (path.equals("/api/health") || path.equals("/api/auth/login")) return true;
        return HttpMethod.GET.matches(request.getMethod()) && path.startsWith("/api/restaurants");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        Optional<Long> userId = Optional.empty();
        if (header != null && header.startsWith("Bearer ")) {
            userId = authService.resolveUserId(header.substring(7).trim());
        }
        if (userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                    new ErrorResponse(401, "Unauthorized", "Missing or invalid token", Instant.now()));
            return;
        }
        request.setAttribute(USER_ID_ATTR, userId.get());
        chain.doFilter(request, response);
    }
}
