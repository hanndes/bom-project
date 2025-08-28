package com.handederelii.bom_project.service;

import com.handederelii.bom_project.dto.DtoUser;
import com.handederelii.bom_project.jwt.AuthRequest;
import com.handederelii.bom_project.jwt.AuthResponse;

public interface IAuthService {
    public DtoUser register(AuthRequest request);
    public AuthResponse authenticate(AuthRequest request);
}
