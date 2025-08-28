package com.handederelii.bom_project.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { //Bu sınıfla controllerlara gelen isteklerde JWT doğrulaması yapılır

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //Bearer Fsjdndksnasd
            String header;
            String token;
            String username;

        header = request.getHeader("Authorization");

        if(header == null){
            filterChain.doFilter(request,response); //filtreleyemedi geri dondu.
            return;
        }
        token = header.substring(7);
        try {
            username = jwtService.getUsernameByToken(token); //
            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if(userDetails != null && jwtService.isTokenExpired(token)){
                        //Token geçerli, SecurityContext'e authentication ekle
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
                        authentication.setDetails(userDetails); //burası controllera girmeyi saglıyor
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
            }
        }catch (ExpiredJwtException e){
            System.out.println("Token suresi dolmustur : " + e.getMessage());

        }catch (Exception e){
            System.out.println("Genel bir hata olustu : " + e.getMessage());
        }
        filterChain.doFilter(request,response); // Zincirdeki bir sonraki filtreye geç
        }
    }

