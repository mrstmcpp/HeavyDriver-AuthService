package org.mrstm.uberauthproject.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.expiry}")
    private int expiry;

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Create a token with payload (claims) + subject (email)
     */
    private String createToken(Map<String, Object> payload, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .claims(payload)
                .issuedAt(now)
                .expiration(expiryDate)
                .subject(username)
                .signWith(key)
                .compact();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            return null; // token invalid or expired
        }
    }

    public <T> T extractFromToken(String token, Function<Claims, T> claimsExtractor) {
        final Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        return claimsExtractor.apply(claims);
    }

    // --- TOKEN GENERATION ---

    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    public String generateToken(Map<String, Object> payload, String username) {
        return createToken(payload, username);
    }

    /**
     * Overloaded version for better readability:
     * Allows direct creation of token with email, role, and userId.
     */
    public String generateToken(String email, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        return createToken(claims, email);
    }

    // --- TOKEN EXTRACTION HELPERS ---

    public String extractEmailFromToken(String token) {
        return extractFromToken(token, Claims::getSubject);
    }

    public String extractRoleFromToken(String token) {
        return extractFromToken(token, claims -> (String) claims.get("role"));
    }

    public Long extractUserIdFromToken(String token) {
        Object userIdObj = extractFromToken(token, claims -> claims.get("userId"));
        if (userIdObj == null) return null;
        return Long.valueOf(userIdObj.toString());
    }

    private Date getExpirationDateFromToken(String token) {
        return extractFromToken(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration == null || expiration.before(new Date());
    }

    public Boolean isTokenValid(String token, String username) {
        try {
            if (username != null && !username.isEmpty()) {
                return username.equals(extractEmailFromToken(token)) && !isTokenExpired(token);
            }
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return false;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
