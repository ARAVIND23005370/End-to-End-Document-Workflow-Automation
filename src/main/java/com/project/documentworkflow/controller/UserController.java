package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.dto.EmailPreferenceRequest;
import com.project.documentworkflow.model.User;
import com.project.documentworkflow.repository.UserRepository;
import com.project.documentworkflow.security.JwtUtil;
import com.project.documentworkflow.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController
 *
 * GET  /api/users                         → List all users (ADMIN only)
 * GET  /api/users/me                      → Get my own profile
 * PUT  /api/users/email-preference        → Set email notification preference (user permission)
 * PUT  /api/users/{id}/role               → Change user role (ADMIN only)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditService auditService;

    // ─── Get all users (ADMIN only) ───
    @GetMapping
    public ApiResponse<List<User>> getAllUsers(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return new ApiResponse<>(false, null, "Access denied. ADMIN role required.");
        }

        return new ApiResponse<>(true, userRepository.findAll(), null);
    }

    // ─── Get my own profile ───
    @GetMapping("/me")
    public ApiResponse<User> getMyProfile(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, null, "User not found");
        }
        return new ApiResponse<>(true, user, null);
    }

    // ─── FEATURE: User sets their email notification preference ───
    @PutMapping("/email-preference")
    public ApiResponse<String> setEmailPreference(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody EmailPreferenceRequest request) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, null, "User not found");
        }

        user.setEmailNotifyOnReject(request.getEmailNotifyOnReject());
        userRepository.save(user);

        String status = Boolean.TRUE.equals(request.getEmailNotifyOnReject()) ? "ENABLED" : "DISABLED";

        auditService.log(null, "EMAIL_PREFERENCE_CHANGED", email,
            "User " + email + " set email notification on reject: " + status);

        return new ApiResponse<>(true, "Email notification preference set to: " + status, null);
    }

    // ─── FEATURE: ADMIN changes user role ───
    @PutMapping("/{userId}/role")
    public ApiResponse<String> changeUserRole(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestParam String newRole) {

        String token = authHeader.replace("Bearer ", "");
        String callerRole = jwtUtil.extractRole(token);
        String callerEmail = jwtUtil.extractEmail(token);

        if (!"ADMIN".equalsIgnoreCase(callerRole)) {
            return new ApiResponse<>(false, null, "Access denied. ADMIN role required.");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, null, "User not found");
        }

        // Valid roles: ADMIN, STAFF, VIEWER
        if (!newRole.equalsIgnoreCase("ADMIN") &&
            !newRole.equalsIgnoreCase("STAFF") &&
            !newRole.equalsIgnoreCase("VIEWER")) {
            return new ApiResponse<>(false, null, "Invalid role. Use: ADMIN, STAFF, VIEWER");
        }

        user.setRole(newRole.toUpperCase());
        userRepository.save(user);

        auditService.log(null, "ROLE_CHANGED", callerEmail,
            "User " + user.getEmail() + " role changed to " + newRole.toUpperCase());

        return new ApiResponse<>(true, "Role updated to: " + newRole.toUpperCase(), null);
    }
}
