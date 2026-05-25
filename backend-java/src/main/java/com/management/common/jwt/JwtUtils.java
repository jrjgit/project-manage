package com.management.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey key;

    public JwtUtils(@Value("${app.jwt-secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                Base64.getEncoder().encodeToString(secret.getBytes())));
    }

    public String generateToken(Long userId, String name, String role, Long groupId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .claim("user_id", userId)
                .claim("name", name)
                .claim("role", role)
                .claim("group_id", groupId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
