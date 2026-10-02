package com.e2edocs;

import com.e2edocs.dto.CreateUserRequest;
import com.e2edocs.dto.UpdateUserRequest;
import com.e2edocs.entity.Organization;
import com.e2edocs.entity.User;
import com.e2edocs.entity.enums.UserRole;
import com.e2edocs.entity.enums.UserStatus;
import com.e2edocs.repository.OrganizationRepository;
import com.e2edocs.repository.UserRepository;
import com.e2edocs.security.JwtService;
import com.e2edocs.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Organization org1;
    private Organization org2;
    private User superAdminUser;
    private User adminUser;
    private User regularUser;
    private User otherOrgUser;

    private String superAdminToken;
    private String adminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        org1 = organizationRepository.save(new Organization("org-test-1", "Test Org 1", "test1", "Primary Org", "test1.com"));
        org2 = organizationRepository.save(new Organization("org-test-2", "Test Org 2", "test2", "Secondary Org", "test2.com"));

        superAdminUser = new User("usr-super", org1.getId(), "Super Admin", "super@test1.com", passwordEncoder.encode("Pass123!"), UserRole.SUPER_ADMIN);
        superAdminUser.setStatus(UserStatus.ACTIVE);
        superAdminUser = userRepository.save(superAdminUser);

        adminUser = new User("usr-admin", org1.getId(), "Admin User", "admin@test1.com", passwordEncoder.encode("Pass123!"), UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);

        regularUser = new User("usr-regular", org1.getId(), "Regular User", "regular@test1.com", passwordEncoder.encode("Pass123!"), UserRole.USER);
        regularUser.setStatus(UserStatus.ACTIVE);
        regularUser = userRepository.save(regularUser);

        otherOrgUser = new User("usr-other", org2.getId(), "Other Org User", "other@test2.com", passwordEncoder.encode("Pass123!"), UserRole.USER);
        otherOrgUser.setStatus(UserStatus.ACTIVE);
        otherOrgUser = userRepository.save(otherOrgUser);

        superAdminToken = jwtService.generateToken(UserPrincipal.create(superAdminUser));
        adminToken = jwtService.generateToken(UserPrincipal.create(adminUser));
        regularUserToken = jwtService.generateToken(UserPrincipal.create(regularUser));
    }

    @Test
    @DisplayName("1. GET /api/users returns only users of the caller's organization")
    void testGetAllUsersOrganizationIsolation() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("super@test1.com", "admin@test1.com", "regular@test1.com")))
                .andExpect(jsonPath("$[*].email", not(hasItem("other@test2.com"))));
    }

    @Test
    @DisplayName("2. POST /api/users/invite creates new user in caller's organization")
    void testInviteUser() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Invited User");
        req.setEmail("invited@test1.com");
        req.setRole(UserRole.REVIEWER);

        mockMvc.perform(post("/api/users/invite")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("invited@test1.com"))
                .andExpect(jsonPath("$.role").value("reviewer"))
                .andExpect(jsonPath("$.status").value("active"));

        assertTrue(userRepository.existsByEmailIgnoreCase("invited@test1.com"));
    }

    @Test
    @DisplayName("3. PUT /api/users/{id} allows ADMIN to deactivate another user")
    void testAdminCanDeactivateUser() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setStatus(UserStatus.INACTIVE);

        mockMvc.perform(put("/api/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("inactive"));

        User updated = userRepository.findById(regularUser.getId()).orElseThrow();
        assertEquals(UserStatus.INACTIVE, updated.getStatus());
    }

    @Test
    @DisplayName("4. PUT /api/users/{id} prevents user from deactivating themselves")
    void testUserCannotDeactivateSelf() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setStatus(UserStatus.INACTIVE);

        mockMvc.perform(put("/api/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("5. PUT /api/users/{id} rejects ADMIN trying to change a role")
    void testAdminCannotChangeRole() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setRole(UserRole.MANAGER);

        mockMvc.perform(put("/api/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6. PUT /api/users/{id} allows SUPER_ADMIN to change a role")
    void testSuperAdminCanChangeRole() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setRole(UserRole.MANAGER);

        mockMvc.perform(put("/api/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("manager"));

        User updated = userRepository.findById(regularUser.getId()).orElseThrow();
        assertEquals(UserRole.MANAGER, updated.getRole());
    }

    @Test
    @DisplayName("7. Organization isolation: Cannot access or update user of another organization")
    void testOrganizationIsolationOnUpdate() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setName("Hacked Name");

        mockMvc.perform(put("/api/users/" + otherOrgUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}
