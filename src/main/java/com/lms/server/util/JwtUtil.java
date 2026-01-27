package com.lms.server.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {

    // ⚠️ Later move this to env variable
    private static final String SECRET =
            "this-is-a-very-secure-secret-key-for-lms-project-12345";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24h
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(UUID employeeId) {
        return Jwts.builder()
                .setSubject(employeeId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static UUID validateAndGetEmployeeId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }
}