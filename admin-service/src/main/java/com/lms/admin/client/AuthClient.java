package com.lms.admin.client;

import com.lms.admin.dto.UserProfileDTO; 
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/api/auth/users")
    List<UserProfileDTO> getAllUsers();

    @PutMapping("/api/auth/users/{id}/status")
    UserProfileDTO toggleUserStatus(@PathVariable("id") Long id, @RequestParam("active") Boolean active);
}