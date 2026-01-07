package com.lms.authservice.service;

import com.lms.authservice.dto.LoginRequestDTO;
import com.lms.authservice.dto.LoginResponseDTO;
import com.lms.authservice.dto.RegisterRequestDTO;
import com.lms.authservice.dto.UserProfileDTO;
import com.lms.authservice.entity.Role;
import com.lms.authservice.entity.User;
import com.lms.authservice.exception.DuplicateUserException;
import com.lms.authservice.exception.InvalidCredentialsException;
import com.lms.authservice.exception.UserNotFoundException;
import com.lms.authservice.repository.UserRepository;
import com.lms.authservice.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.CUSTOMER);

        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("Password@123");
        registerRequest.setEmail("test@test.com");
        registerRequest.setFullName("Test User");
        registerRequest.setRole("CUSTOMER");

        loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password@123");
    }

    @Test
    void register_ShouldSaveUser_WhenUserIsNew() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        LoginResponseDTO response = authService.register(registerRequest);

        verify(userRepository, times(1)).save(any(User.class));
        assertNotNull(response);
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn("mock-jwt-token");

        LoginResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsInvalid() {
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void getUserProfile_ShouldReturnProfile_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserProfileDTO profile = authService.getUserProfile(1L);

        assertNotNull(profile);
        assertEquals("test@test.com", profile.getEmail());
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.getUserProfile(99L));
    }

    @Test
    void updateUser_ShouldUpdateDetails_WhenUserExists() {
        RegisterRequestDTO updateRequest = new RegisterRequestDTO();
        updateRequest.setFullName("Updated Name");
        updateRequest.setEmail("new@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserProfileDTO updatedProfile = authService.updateUser(1L, updateRequest);

        assertNotNull(updatedProfile);
        assertEquals("Updated Name", testUser.getFullName());
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.updateUser(99L, registerRequest));
    }
}
