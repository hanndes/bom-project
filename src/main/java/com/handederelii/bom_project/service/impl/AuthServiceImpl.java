package com.handederelii.bom_project.service.impl;

import com.handederelii.bom_project.dto.DtoUser;
import com.handederelii.bom_project.entity.RefreshToken;
import com.handederelii.bom_project.entity.User;
import com.handederelii.bom_project.exceptions.AuthException;
import com.handederelii.bom_project.jwt.AuthRequest;
import com.handederelii.bom_project.jwt.AuthResponse;
import com.handederelii.bom_project.jwt.JwtService;
import com.handederelii.bom_project.repositories.RefreshTokenRepository;
import com.handederelii.bom_project.repositories.UserRepository;
import com.handederelii.bom_project.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    private RefreshToken createRefreshToken(User user)
    {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpireDate(new Date(System.currentTimeMillis()+ 1000 * 60 * 60 * 4));
        refreshToken.setUser(user);
        return refreshToken;
    }
    @Override
    public AuthResponse authenticate(AuthRequest request) {
        try{
           UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
           authenticationProvider.authenticate(auth);
           Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
           String accessToken = jwtService.generateToken(optionalUser.get());
           RefreshToken refreshToken = createRefreshToken(optionalUser.get());
           refreshTokenRepository.save(refreshToken);
           return new AuthResponse(accessToken,refreshToken.getToken());
        }catch(Exception e){
            throw new AuthException("Kullanıcı adı veya şifre hatalı");
        }
    }
    @Override
    public DtoUser register(AuthRequest request) {
        DtoUser dtoUser = new DtoUser();
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);
        BeanUtils.copyProperties(savedUser,dtoUser);
        return dtoUser;
    }

}
