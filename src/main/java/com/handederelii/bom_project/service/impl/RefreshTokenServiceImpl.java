package com.handederelii.bom_project.service.impl;

import com.handederelii.bom_project.entity.RefreshToken;
import com.handederelii.bom_project.entity.User;
import com.handederelii.bom_project.exceptions.TokenExpiredException;
import com.handederelii.bom_project.jwt.AuthResponse;
import com.handederelii.bom_project.jwt.JwtService;
import com.handederelii.bom_project.jwt.RefreshTokenRequest;
import com.handederelii.bom_project.repositories.RefreshTokenRepository;
import com.handederelii.bom_project.service.IRefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public boolean isRefreshTokenExpired(Date expiredDate) {
        return new Date().after(expiredDate);
    }
    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpireDate(new Date(System.currentTimeMillis()+ 1000*60*60*4));
        refreshToken.setUser(user);

        return refreshToken;
    }

    //sjkfaskf ksjf askjf aksjf kjsldfkjl
    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        Optional<RefreshToken> optional = refreshTokenRepository.findByToken(request.getRefreshToken());
        if(optional.isEmpty()) {
            log.warn("Refresh token süresi dolmuş. token={}", request.getRefreshToken());
        }

        RefreshToken refreshToken = optional.get();

        if(isRefreshTokenExpired(refreshToken.getExpireDate())) {
            log.warn("Refresh token süresi dolmuş. token={}", request.getRefreshToken());
            throw new TokenExpiredException("Refresh token expired: " + request.getRefreshToken());
        }


        String accessToken = jwtService.generateToken(refreshToken.getUser());
        RefreshToken savedRefreshToken= refreshTokenRepository.save(createRefreshToken(refreshToken.getUser()));

        // accesss 2
        // refresh 1

        return new AuthResponse(accessToken, savedRefreshToken.getToken());
    }

}
