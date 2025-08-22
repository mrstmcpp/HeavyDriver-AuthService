package org.mrstm.uberauthproject.services;



import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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


    private String createToken(Map<String , Object> payload , String username){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry);
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder().claims(payload)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiryDate)
                .subject(username)
                .signWith(key)
                .compact();
    }

    private SecretKey getSignInKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims extractAllClaims(String token){
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            //
            return null;
        }
    }

    public <T> T extractFromToken(String token , Function<Claims , T> claimsExtractor){
        //here claimsextractor would be a function which will be passed inorder to get required data from token.
        final Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        return claimsExtractor.apply(claims);
    }


    public String generateToken(String username){
        return generateToken(new HashMap<>(), username);
    }

    /**
     * above function is created as it is more readable while calling this function in any component
     * String token = jwtService.generateToken(userDetails);
     *String token = jwtService.generateToken(new HashMap<>(), userDetails);
     *
     * 1st one is more clean.
     * @param payload
     * @param username
     * @return
     */
    public String generateToken(Map<String , Object> payload , String username){
        return createToken(payload,username);
    }

    public String extractEmailFromToken(String token){
        return extractFromToken(token, Claims::getSubject);
    }

    public String extractUserIdFromToken(String token){
        return extractFromToken(token, Claims::getId);
    }

    private Date getExpirationDateFromToken(String token){
        return extractFromToken(token , Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token){
        //returns true if expired...
        return getExpirationDateFromToken(token).before(new Date());
    }

    public Boolean isTokenValid(String token, String username) {
        try {
            if (username != null && !username.isEmpty()) {
                return username.equals(extractEmailFromToken(token)) && !isTokenExpired(token);
            }
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token expired
            return false;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            // unsupported or null token
            return false;
        }
    }



//    @Override
//    public void run(String... args) throws Exception {
//        Map<String , Object> payload = new HashMap<>();
//        payload.put("username", "john");
//        payload.put("password", "secret");
//        payload.put("role", "admin");
//        String token = createToken(payload , "john");
//        System.out.println(token);
//
//    }
}
