package com.ntnt.httpserver.services;

import com.ntnt.httpserver.models.UserEntity;
import io.jsonwebtoken.*;

import java.util.Date;

public class JwtService {
    private final String secretKey = "HiHiHiHaHaHa";
    private final long expiration =8640000000L;

    public String generateToken(String subject, String issuer) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public UserEntity getUserFromToken(String token){
        try{
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(claims.getIssuer());
            userEntity.setCrawledWebCookie(claims.getSubject());

            return userEntity;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean isValidatedToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            System.out.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.out.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.out.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.out.println("JWT claims string is empty.");
        }
        return false;
    }
}
