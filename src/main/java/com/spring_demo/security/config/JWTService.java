package com.spring_demo.security.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.jsonwebtoken.Jwts.*;

@Service
public class JWTService {

    private static final String SECRET_KEY = "a3f57e5b4ef5b5c1b1cc2d4c9e4e4f0a5d6fb3a3324e9d1dbb7f5e1b7e1c2f8d";

    public String extractUsername
            (
                    String token
            ) {
        return extractClaim(token, Claims::getSubject);
    }


    public <T> T extractClaim
            (
                    String token,
                    Function<Claims, T> claimsResolver
            ) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);

    }

    public String generateToken
            (
                    UserDetails userDetails
            ) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken
            (
                    Map<String, Object> extraClaims,
                    UserDetails userDetails
            ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public boolean isTokenValid
            (
                    String token, UserDetails userDetails
            ) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired
            (
                    String token
            ) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration
            (
                    String token
            ) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims
            (
                    String token
            ) {
        return parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSignInKey
            () {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
