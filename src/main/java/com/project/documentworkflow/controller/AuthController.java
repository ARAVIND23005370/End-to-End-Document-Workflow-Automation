package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.dto.LoginRequest;
import com.project.documentworkflow.dto.RegisterRequest;
import com.project.documentworkflow.model.User;
import com.project.documentworkflow.repository.UserRepository;
import com.project.documentworkflow.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ApiResponse<>(false, null, "Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);

        return new ApiResponse<>(true, "User registered successfully", null);
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null ||
                !passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            return new ApiResponse<>(false, null, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return new ApiResponse<>(true, token, null);
    }
}