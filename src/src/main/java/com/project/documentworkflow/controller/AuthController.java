package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestParam String username,
                                     @RequestParam String password) {

        if (username.equals("admin") && password.equals("admin123")) {

            String token = jwtService.generateToken(username);

            return new ApiResponse<>(true, token, null);
        }

        return new ApiResponse<>(false, null, "Invalid credentials");
    }
}
