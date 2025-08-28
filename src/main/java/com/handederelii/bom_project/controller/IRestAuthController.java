package com.handederelii.bom_project.controller;

import com.handederelii.bom_project.dto.DtoUser;
import com.handederelii.bom_project.jwt.AuthRequest;
import com.handederelii.bom_project.jwt.AuthResponse;

public interface IRestAuthController {
    public DtoUser register(AuthRequest request);
    public AuthResponse authenticate(AuthRequest request);
}
