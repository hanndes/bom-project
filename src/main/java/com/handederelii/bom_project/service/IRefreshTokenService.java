package com.handederelii.bom_project.service;

import com.handederelii.bom_project.jwt.AuthResponse;
import com.handederelii.bom_project.jwt.RefreshTokenRequest;

public interface IRefreshTokenService {
public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}
