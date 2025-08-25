package com.handederelii.bom_project.controller;

import com.handederelii.bom_project.dto.request.LoginRequest;
import com.handederelii.bom_project.dto.request.RegisterRequest;
import com.handederelii.bom_project.dto.response.JwtResponse;
import com.handederelii.bom_project.dto.response.UserResponse;
import com.handederelii.bom_project.service.AuthService;
import com.handederelii.bom_project.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse=authService.register(request.email(), request.password());
        return ResponseEntity.ok(userResponse); // 201 Created
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        authService.validateCredentials(request.email(), request.password());
        String token = jwtService.generateToken(request.email());
        return ResponseEntity.ok(new JwtResponse(token, jwtService.getTtlSeconds()));
    }
}
