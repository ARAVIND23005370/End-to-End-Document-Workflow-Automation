package com.e2edocs.service;

import com.e2edocs.dto.*;
import com.e2edocs.entity.Organization;
import com.e2edocs.entity.PasswordResetToken;
import com.e2edocs.entity.User;
import com.e2edocs.entity.enums.AuditAction;
import com.e2edocs.entity.enums.AuditStatus;
import com.e2edocs.entity.enums.UserRole;
import com.e2edocs.entity.enums.UserStatus;
import com.e2edocs.exception.BadRequestException;
import com.e2edocs.exception.ResourceNotFoundException;
import com.e2edocs.repository.OrganizationRepository;
import com.e2edocs.repository.PasswordResetTokenRepository;
import com.e2edocs.repository.UserRepository;
import com.e2edocs.security.JwtService;
import com.e2edocs.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final Object BOOTSTRAP_LOCK = new Object();

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final org.springframework.transaction.PlatformTransactionManager transactionManager;

    public AuthService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuditService auditService,
            org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.transactionManager = transactionManager;
    }

    public SignupStatusResponse getSignupStatus() {
        boolean available = userRepository.count() == 0;
        return new SignupStatusResponse(available);
    }

    public AuthResponse register(RegisterRequest request, String ipAddress) {
        synchronized (BOOTSTRAP_LOCK) {
            org.springframework.transaction.support.TransactionTemplate transactionTemplate =
                    new org.springframework.transaction.support.TransactionTemplate(transactionManager);

            return transactionTemplate.execute(status -> {
                if (userRepository.count() > 0) {
                    throw new BadRequestException("Public registration is disabled. The system administrator setup is already complete.");
                }

                String normalizedEmail = request.getEmail().trim().toLowerCase();
                String normalizedName = request.getName().trim();
                String normalizedOrgName = request.getOrganizationName().trim();

                if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                    throw new BadRequestException("An account with this email address already exists.");
                }

                // Generate organization
                String orgId = "org-" + UUID.randomUUID().toString().substring(0, 8);
                String baseCode = normalizedOrgName.toLowerCase()
                        .replaceAll("[^a-z0-9]", "-")
                        .replaceAll("-+", "-")
                        .replaceAll("^-|-$", "");
                if (baseCode.isEmpty()) {
                    baseCode = "org";
                }
                String orgCode = baseCode;
                int counter = 1;
                while (organizationRepository.existsByCode(orgCode)) {
                    orgCode = baseCode + "-" + counter++;
                }

                String domain = null;
                if (normalizedEmail.contains("@")) {
                    domain = normalizedEmail.substring(normalizedEmail.indexOf('@') + 1);
                }

                Organization organization = new Organization(orgId, normalizedOrgName, orgCode, "Organization for " + normalizedOrgName, domain);
                organization = organizationRepository.save(organization);

                // Create initial Main Admin user with ADMIN role
                String userId = "usr-" + UUID.randomUUID().toString().substring(0, 8);
                String encodedPassword = passwordEncoder.encode(request.getPassword());
                User user = new User(userId, organization.getId(), normalizedName, normalizedEmail, encodedPassword, UserRole.ADMIN);
                user.setStatus(UserStatus.ACTIVE);
                user = userRepository.save(user);

                auditService.log(organization.getId(), user.getId(), user.getName(), AuditAction.CREATED,
                        "Auth", user.getId(), AuditStatus.SUCCESS, "Initial Main Admin bootstrap registration: " + organization.getName(), ipAddress);

                UserPrincipal userPrincipal = UserPrincipal.create(user);
                String token = jwtService.generateToken(userPrincipal);

                return new AuthResponse(mapToUserResponse(user), token);
            });
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(userPrincipal);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastActiveAt(Instant.now());
        userRepository.save(user);

        auditService.log(user.getOrganizationId(), user.getId(), user.getName(), AuditAction.CREATED,
                "Auth", user.getId(), AuditStatus.SUCCESS, "User login successful: " + user.getEmail(), ipAddress);

        return new AuthResponse(mapToUserResponse(user), token);
    }

    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userPrincipal.getId()));
        return mapToUserResponse(user);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElse(null);
        if (user != null) {
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = hashToken(rawToken);
            Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);

            PasswordResetToken resetToken = new PasswordResetToken(hashedToken, user.getId(), expiresAt);
            passwordResetTokenRepository.save(resetToken);

            logger.info("Password reset requested for {}. In local dev mode, generated token: {}", user.getEmail(), rawToken);
        }

        // Return generic success to prevent email enumeration
        return new MessageResponse("If an account with that email exists, password reset instructions have been generated.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String hashedToken = hashToken(request.getToken());
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedFalseAndExpiresAtAfter(hashedToken, Instant.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        auditService.log(user.getOrganizationId(), user.getId(), user.getName(), AuditAction.UPDATED,
                "Auth", user.getId(), AuditStatus.SUCCESS, "Password reset successfully completed", "SYSTEM");

        return new MessageResponse("Password has been reset successfully. You may now log in.");
    }

    @Transactional
    public MessageResponse changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.log(user.getOrganizationId(), user.getId(), user.getName(), AuditAction.UPDATED,
                "Auth", user.getId(), AuditStatus.SUCCESS, "User changed account password", "SYSTEM");

        return new MessageResponse("Password changed successfully");
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getDepartment(),
                user.getDepartmentId(),
                user.getStatus(),
                user.getAvatar(),
                user.getLastActiveAt(),
                user.getCreatedAt()
        );
    }
}
