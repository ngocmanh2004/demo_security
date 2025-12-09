package com.demo.security.controller;

import com.demo.security.dto.*;
import com.demo.security.entity.RefreshToken;
import com.demo.security.entity.Role;
import com.demo.security.entity.User;
import com.demo.security.security.JwtUtil;
import com.demo.security.service.CustomUserDetailsService;
import com.demo.security.service.RefreshTokenService;
import com.demo.security.service.RoleService;
import com.demo.security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final RoleService roleService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Load user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate access token
            String accessToken = jwtUtil.generateAccessToken(userDetails);

            // Create and save refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

            // Extract roles
            String[] roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray(String[]::new);

            // Build response
            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .accessTokenExpiresIn(jwtUtil.getAccessTokenExpiration())
                    .refreshTokenExpiresIn(jwtUtil.getRefreshTokenExpiration())
                    .username(userDetails.getUsername())
                    .roles(roles)
                    .build();

            return ResponseEntity.ok(tokenResponse);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDTO registrationDto) {
        try {
            // Check if username exists
            if (userService.existsByUsername(registrationDto.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Username already exists"));
            }

            // Check if email exists
            if (userService.existsByEmail(registrationDto.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Email already registered"));
            }

            // Check if passwords match
            if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Passwords do not match"));
            }

            // Create new user
            User user = User.builder()
                    .username(registrationDto.getUsername())
                    .password(registrationDto.getPassword())
                    .email(registrationDto.getEmail())
                    .fullName(registrationDto.getFullName())
                    .enabled(true)
                    .build();

            // Assign USER role
            Role userRole = roleService.findByRoleName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);

            // Save user
            userService.saveUser(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(MessageResponse.builder()
                            .message("User registered successfully")
                            .status(201)
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String requestRefreshToken = request.getRefreshToken();

            // Find and verify refresh token
            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            refreshToken = refreshTokenService.verifyExpiration(refreshToken);

            // Load user details
            User user = refreshToken.getUser();
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(userDetails);

            // Extract roles
            String[] roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray(String[]::new);

            // Build response
            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(requestRefreshToken)
                    .tokenType("Bearer")
                    .accessTokenExpiresIn(jwtUtil.getAccessTokenExpiration())
                    .refreshTokenExpiresIn(jwtUtil.getRefreshTokenExpiration())
                    .username(user.getUsername())
                    .roles(roles)
                    .build();

            return ResponseEntity.ok(tokenResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Token refresh failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            refreshTokenService.deleteByToken(refreshToken);

            return ResponseEntity.ok(new MessageResponse("Logout successful"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Logout failed: " + e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid token format"));
            }

            String token = authHeader.substring(7);

            if (jwtUtil.validateTokenStructure(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                return ResponseEntity.ok(MessageResponse.builder()
                        .message("Token is valid")
                        .data(username)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid token"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Token validation failed"));
        }
    }
}
