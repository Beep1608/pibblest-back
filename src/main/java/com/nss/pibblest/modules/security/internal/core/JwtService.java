package com.nss.pibblest.modules.security.internal.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    
    @Value("${security.jwt.secret-key}")
    private String secretKey;


    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String generateToken(UUID employeeId, String username, String owner){

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("employeeId", employeeId);
        extraClaims.put("username", username);

        if(owner != null && !owner.isBlank()){
            extraClaims.put("owner", owner);
        }

        return  Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();

    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public String extractOwner(String token){
        return extractAllClaims(token).get("owner", String.class);
    }

    private boolean isTokenValid(String token, String username){
        final String extractedUsername = extractUsername(token);

        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired (String token) {
         return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration (String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim (String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims (String token){
        return Jwts.parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }   

    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
