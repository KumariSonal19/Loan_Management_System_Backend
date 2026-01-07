package com.lms.admin.controller;

import com.lms.admin.client.AuthClient;
import com.lms.admin.dto.UserProfileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AuthClient authClient;

    @GetMapping
    public ResponseEntity<List<UserProfileDTO>> getUsers() {
        return ResponseEntity.ok(authClient.getAllUsers());
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<UserProfileDTO> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(authClient.toggleUserStatus(id, true));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<UserProfileDTO> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(authClient.toggleUserStatus(id, false));
    }
}