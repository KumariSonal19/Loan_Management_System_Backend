package com.lms.authservice.service;

import com.lms.authservice.dto.*;
import com.lms.authservice.entity.Role;
import com.lms.authservice.entity.User;
import com.lms.authservice.exception.*;
import com.lms.authservice.repository.UserRepository;
import com.lms.authservice.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDTO register(RegisterRequestDTO request) {

        validateNewUser(request);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.valueOf(request.getRole()))
                .isActive(true)
                .build();

        userRepository.save(user);

        return buildLoginResponse(user);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("User account is disabled");
        }

        return buildLoginResponse(user);
    }

    public UserProfileDTO getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserProfileDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .active(user.getIsActive())
                .build();
    }

    public UserProfileDTO updateUser(Long id, RegisterRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);

        return getUserProfile(id);
    }

    private LoginResponseDTO buildLoginResponse(User user) {
        return LoginResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .accessToken(jwtTokenProvider.generateToken(user))
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .build();
    }

    private void validateNewUser(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }
    }
    
 public List<UserProfileDTO> getAllUsers() {
     return userRepository.findAll().stream()
             .map(user -> UserProfileDTO.builder()
                     .userId(user.getId())
                     .username(user.getUsername())
                     .email(user.getEmail())
                     .fullName(user.getFullName())
                     .phoneNumber(user.getPhoneNumber())
                     .role(user.getRole().name())
                     .active(user.getIsActive())
                     .build())
             .collect(Collectors.toList());
 }

 public UserProfileDTO toggleUserStatus(Long userId, Boolean isActive) {
     User user = userRepository.findById(userId)
             .orElseThrow(() -> new UserNotFoundException("User not found"));
     
     user.setIsActive(isActive);
     userRepository.save(user);
     
     return getUserProfile(userId); 
 }
}
