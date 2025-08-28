package com.handederelii.bom_project.controller.impl;

import com.handederelii.bom_project.controller.IRestAuthController;
import com.handederelii.bom_project.dto.DtoUser;
import com.handederelii.bom_project.jwt.AuthRequest;
import com.handederelii.bom_project.jwt.AuthResponse;
import com.handederelii.bom_project.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RestAuthControllerImpl implements IRestAuthController {

    private final IAuthService authService;

    @Override
    @PostMapping("/register")
    public DtoUser register( @Valid @RequestBody AuthRequest request) {
        return authService.register(request);
    }
    @PostMapping("/login")
    @Override
    public AuthResponse authenticate(@Valid @RequestBody AuthRequest request) {
        return authService.authenticate(request);
    }
}
