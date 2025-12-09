package com.demo.security.security;

import com.demo.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Paths that don't need JWT authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/**",
            "/api/public/**",
            "/",
            "/index",
            "/index.html",
            "/login",
            "/login.html",
            "/register",
            "/register.html",
            "/products",
            "/products.html",
            "/admin",
            "/admin.html",
            "/favicon.ico",
            "/css/**",
            "/js/**",
            "/images/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Log for debugging
        log.debug("Processing request: {} {}", request.getMethod(), path);

        // Skip JWT validation for public paths
        if (isPublicPath(path)) {
            log.debug("Public path, skipping JWT validation: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null) {
                log.debug("JWT token found, validating...");

                if (jwtUtil.validateTokenStructure(jwt)) {
                    String username = jwtUtil.getUsernameFromToken(jwt);
                    log.debug("Token username: {}", username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        log.debug("User loaded: {}, authorities: {}", username, userDetails.getAuthorities());

                        if (jwtUtil.validateToken(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Authentication set for user: {}", username);
                        } else {
                            log.warn("Token validation failed for user: {}", username);
                        }
                    }
                } else {
                    log.warn("Invalid token structure");
                }
            } else {
                log.debug("No JWT token found in request to: {}", path);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication for path {}: {}", path, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}