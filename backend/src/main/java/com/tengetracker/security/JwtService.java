package com.tengetracker.security;

import com.tengetracker.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Creates and verifies HS256-signed JSON Web Tokens.
 *
 * Token = base64url(header) . base64url(payload) . signature
 *  header : {"alg":"HS256"}
 *  payload: {"sub":"<userId>","username":"...","ver":<tokenVersion>,"iat":...,"exp":...}
 *  signature = HMAC-SHA256(header + "." + payload, JWT_SECRET)
 *
 * The payload is only encoded, not encrypted: never put secrets in it.
 */
@Service
public class JwtService {

    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_VERSION = "ver";

    private final SecretKey key;
    private final Duration expiration;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes long for HS256");
        }
        this.key = new SecretKeySpec(bytes, "HmacSHA256");
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_VERSION, user.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Verifies the signature and expiry and returns the claims.
     *
     * @throws io.jsonwebtoken.ExpiredJwtException if the token is expired
     * @throws JwtException                        if the token is malformed or the signature is wrong
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }
}
