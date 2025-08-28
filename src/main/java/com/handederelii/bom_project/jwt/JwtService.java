package com.handederelii.bom_project.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-ttl:3600}") long ttlSeconds
    ) {
        // secret en az 32 byte olmalı
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000*5))
                .signWith(this.key, SignatureAlgorithm.HS256)
                .compact();
    }

    public <T> T exportToken(String token, Function<Claims,T> claimsFunction){ // token içinden istediğin bilgiyi almanı sağlar
        Claims claims=Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
         return claimsFunction.apply(claims);
    }

    public String getUsernameByToken(String token){
       return exportToken(token,Claims::getSubject);
    }

    public boolean isTokenExpired(String token){
        Date expiredData = exportToken(token,Claims::getExpiration);
        return expiredData.before(new Date());
    }
}
