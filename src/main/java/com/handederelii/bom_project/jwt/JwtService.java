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
import java.util.function.Function;


@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long ttlSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-ttl:3600}") long ttlSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
        //        .addClaims(claimsMap)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ttlSeconds * 1000 ))
                .signWith(this.secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Object getClaimsByKey(String token, String key){
        Claims claims = getClaims(token);
        return claims.get(key);

    }

    public Claims getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T exportToken(String token, Function<Claims,T> claimsFunction){ // token içinden istediğin bilgiyi almanı sağlar
        Claims claims=getClaims(token);
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
