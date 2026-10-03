package com.e2edocs.controller;

import com.e2edocs.dto.CreateUserRequest;
import com.e2edocs.dto.UpdateUserRequest;
import com.e2edocs.dto.UserResponse;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<UserResponse> users = userService.getAllUsers(orgId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        UserResponse user = userService.getUserById(id, orgId);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        UserResponse user = userService.createUser(orgId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/invite")
    public ResponseEntity<UserResponse> inviteUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        UserResponse user = userService.createUser(orgId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponse user = userService.updateUser(id, request, principal);
        return ResponseEntity.ok(user);
    }
}
