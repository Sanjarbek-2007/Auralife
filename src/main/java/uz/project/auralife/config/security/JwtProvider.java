package uz.project.auralife.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uz.project.auralife.domains.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.StringJoiner;

@Component
public class JwtProvider {
    @Value("${secret.key}")
    private String secretKey;

    public String generate(User user, String iotDeviceId) {
        StringJoiner roles = new StringJoiner(",");

        if (user.getRoles() == null) {
            throw new IllegalStateException("User roles are not set.");
        }

        user.getRoles().forEach(role -> roles.add(role.getName().toUpperCase()));

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) // 30 days
                .claim("roles", roles.toString())
                .claim("iot_device_id", iotDeviceId) // 👈 attach the device
                .signWith(key())
                .compact();
    }


    private SecretKey key() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public Claims parse(String token){
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validate(final String token) {
        try {
            Claims claims = parse(token);
            if (claims.getExpiration().after(new Date())) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    public String getIotDeviceId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())   // verify with same key you signed
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // return claim value as String
        return claims.get("iot_device_id", String.class);
    }
}





