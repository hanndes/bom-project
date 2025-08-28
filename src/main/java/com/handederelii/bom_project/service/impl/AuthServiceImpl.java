package com.handederelii.bom_project.service.impl;

import com.handederelii.bom_project.dto.DtoUser;
import com.handederelii.bom_project.entity.User;
import com.handederelii.bom_project.jwt.AuthRequest;
import com.handederelii.bom_project.jwt.AuthResponse;
import com.handederelii.bom_project.jwt.JwtService;
import com.handederelii.bom_project.repositories.UserRepository;
import com.handederelii.bom_project.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final  BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        try{
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            authenticationProvider.authenticate(auth);
            Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
           String token = jwtService.generateToken(optionalUser.get());
            return new AuthResponse(token);
        }catch(Exception e){
           System.out.println("Kullanıcı adı veya şifre hatalı");
        }
        return null;
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
