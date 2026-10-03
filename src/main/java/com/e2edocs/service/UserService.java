package com.e2edocs.service;

import com.e2edocs.dto.CreateUserRequest;
import com.e2edocs.dto.UpdateUserRequest;
import com.e2edocs.dto.UserResponse;
import com.e2edocs.entity.Department;
import com.e2edocs.entity.User;
import com.e2edocs.entity.enums.AuditAction;
import com.e2edocs.entity.enums.AuditStatus;
import com.e2edocs.entity.enums.UserRole;
import com.e2edocs.entity.enums.UserStatus;
import com.e2edocs.exception.BadRequestException;
import com.e2edocs.exception.ResourceNotFoundException;
import com.e2edocs.repository.DepartmentRepository;
import com.e2edocs.repository.OrganizationRepository;
import com.e2edocs.repository.PasswordResetTokenRepository;
import com.e2edocs.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final com.e2edocs.service.email.EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${e2edocs.app.frontend-url:http://localhost:5173}")
    private String frontendUrl = "http://localhost:5173";

    public UserService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            OrganizationRepository organizationRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            com.e2edocs.service.email.EmailService emailService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(String organizationId) {
        return userRepository.findByOrganizationId(organizationId).stream()
                .map(AuthService::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return AuthService.mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String id, String organizationId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        if (organizationId != null && !organizationId.equals(user.getOrganizationId())) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        return AuthService.mapToUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(String organizationId, CreateUserRequest request) {
        return createUser(organizationId, request, null);
    }

    @Transactional
    public UserResponse createUser(String organizationId, CreateUserRequest request, com.e2edocs.security.UserPrincipal inviterPrincipal) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("User with email " + request.getEmail() + " already exists");
        }

        String userId = "u-" + UUID.randomUUID().toString().substring(0, 8);
        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : UUID.randomUUID().toString().substring(0, 12);

        User user = new User(
                userId,
                organizationId,
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(rawPassword),
                request.getRole() != null ? request.getRole() : UserRole.USER
        );

        if (request.getDepartmentId() != null) {
            user.setDepartmentId(request.getDepartmentId());
            departmentRepository.findById(request.getDepartmentId())
                    .ifPresent(d -> user.setDepartment(d.getName()));
        } else if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }

        User saved = userRepository.save(user);

        // Generate invitation & password setup token (valid for 72 hours)
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = AuthService.hashToken(rawToken);
        java.time.Instant expiresAt = java.time.Instant.now().plus(72, java.time.temporal.ChronoUnit.HOURS);
        passwordResetTokenRepository.save(new com.e2edocs.entity.PasswordResetToken(hashedToken, saved.getId(), expiresAt));

        String baseUrl = (frontendUrl != null && !frontendUrl.isBlank()) ? frontendUrl.replaceAll("/+$", "") : "http://localhost:5173";
        String setupUrl = baseUrl + "/reset-password?token=" + rawToken + "&email=" + saved.getEmail();

        String orgName = organizationRepository.findById(organizationId)
                .map(com.e2edocs.entity.Organization::getName)
                .orElse("E2EDocs Workspace");

        String inviterName = (inviterPrincipal != null && inviterPrincipal.getName() != null && !inviterPrincipal.getName().isBlank())
                ? inviterPrincipal.getName()
                : "Workspace Administrator";
        String inviterEmail = (inviterPrincipal != null && inviterPrincipal.getEmail() != null && !inviterPrincipal.getEmail().isBlank())
                ? inviterPrincipal.getEmail()
                : "admin@e2edocs.com";

        emailService.sendInvitationEmail(
                saved.getEmail(),
                saved.getName(),
                inviterName,
                inviterEmail,
                orgName,
                saved.getRole().name(),
                setupUrl
        );

        auditService.log(organizationId, saved.getId(), saved.getName(), AuditAction.CREATED,
                "User", saved.getId(), AuditStatus.SUCCESS, "Created and invited user: " + saved.getEmail() + " by " + inviterName, "SYSTEM");

        return AuthService.mapToUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        return updateUser(id, request, null);
    }

    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request, com.e2edocs.security.UserPrincipal principal) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (principal != null && !principal.getOrganizationId().equals(user.getOrganizationId())) {
            throw new ResourceNotFoundException("User not found: " + id);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            String normalizedEmail = request.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new BadRequestException("Email already taken: " + request.getEmail());
            }
            user.setEmail(normalizedEmail);
        }

        // Role change permission: ONLY SUPER_ADMIN can modify user roles
        if (request.getRole() != null && request.getRole() != user.getRole()) {
            if (principal != null && principal.getRole() != UserRole.SUPER_ADMIN) {
                throw new BadRequestException("Only SUPER_ADMIN can modify user roles.");
            }
            user.setRole(request.getRole());
        }

        // Self-deactivation protection
        if (request.getStatus() != null) {
            if (request.getStatus() == UserStatus.INACTIVE && principal != null && principal.getId().equals(user.getId())) {
                throw new BadRequestException("Cannot deactivate your own active user account.");
            }
            user.setStatus(request.getStatus());
        }

        if (request.getDepartmentId() != null) {
            user.setDepartmentId(request.getDepartmentId());
            departmentRepository.findById(request.getDepartmentId())
                    .ifPresent(d -> user.setDepartment(d.getName()));
        } else if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        if (request.getPreferences() != null) {
            user.setPreferences(request.getPreferences());
        }

        User saved = userRepository.save(user);

        auditService.log(saved.getOrganizationId(), saved.getId(), saved.getName(), AuditAction.UPDATED,
                "User", saved.getId(), AuditStatus.SUCCESS, "Updated user: " + saved.getEmail(), "SYSTEM");

        return AuthService.mapToUserResponse(saved);
    }
}
