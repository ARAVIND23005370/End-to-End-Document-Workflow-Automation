package com.e2edocs.controller;

import com.e2edocs.dto.*;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/signup/status")
    public ResponseEntity<SignupStatusResponse> getSignupStatus() {
        SignupStatusResponse status = authService.getSignupStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        String ipAddress = servletRequest.getRemoteAddr();
        AuthResponse response = authService.register(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String ipAddress = servletRequest.getRemoteAddr();
        AuthResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok(new MessageResponse("Successfully logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = authService.getCurrentUser(principal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse response = authService.changePassword(principal, request);
        return ResponseEntity.ok(response);
    }
    /**
     * DEV ONLY: Force-reset a user password. Remove this endpoint after bootstrapping.
     * POST /api/auth/dev-reset  { "email": "...", "password": "..." }
     */
    @PostMapping("/dev-reset")
    public ResponseEntity<MessageResponse> devReset(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        MessageResponse response = authService.devResetPassword(email, password);
        return ResponseEntity.ok(response);
    }
}
