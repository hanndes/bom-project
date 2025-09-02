package com.handederelii.bom_project.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
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

        // Header yoksa ya da "Bearer " ile başlamıyorsa token alma, zincire devam et
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("Authorization header bulunamadı veya 'Bearer ' ile başlamıyor, filtre zincirine devam ediliyor.");
            filterChain.doFilter(request, response);
            return;
        }
        token = header.substring(7); // "Bearer " sonrası
        try {
            username = jwtService.getUsernameByToken(token); //
            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if(userDetails != null && !jwtService.isTokenExpired(token)){
                        //Token geçerli, SecurityContext'e authentication ekle
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
                        authentication.setDetails(userDetails); //burası controllera girmeyi saglıyor
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
            }
        }catch (ExpiredJwtException e){
            log.warn("Token süresi dolmuş: {}" + e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token formatı hatalı", e);
        }catch (Exception e){
            log.error("Genel JWT hatası", e);
        }
        filterChain.doFilter(request,response); // Zincirdeki bir sonraki filtreye geç
        }
    }

