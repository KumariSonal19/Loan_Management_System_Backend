package com.lms.authservice.controller;

import com.lms.authservice.dto.LoginRequestDTO;
import com.lms.authservice.dto.LoginResponseDTO;
import com.lms.authservice.dto.RegisterRequestDTO;
import com.lms.authservice.dto.UserProfileDTO;
import com.lms.authservice.entity.User;
import com.lms.authservice.service.AuthService;
import com.lms.authservice.util.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("Register request for user: {}", request.getUsername());
        
        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Login request for user: {}", request.getUsername());
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserProfile(id));
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @PathVariable Long id,
            @Valid @RequestBody RegisterRequestDTO request) {

        return ResponseEntity.ok(authService.updateUser(id, request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
    
 @GetMapping("/users")
 public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
     return ResponseEntity.ok(authService.getAllUsers());
 }

 @PutMapping("/users/{id}/status")
 public ResponseEntity<UserProfileDTO> toggleUserStatus(
         @PathVariable Long id, 
         @RequestParam Boolean active) {
     return ResponseEntity.ok(authService.toggleUserStatus(id, active));
 }
}
