package uz.quizplatform.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * JWT utility — validates tokens and extracts claims.
 *
 * Uses HMAC-SHA256 with a shared secret for now.
 * Production upgrade: switch to RS256 with a private/public key pair
 * and store keys in Kubernetes Secrets or a secrets manager.
 *
 * The gateway uses this to validate tokens before forwarding requests.
 * It passes userId downstream as X-User-Id header.
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey signingKey;

    public JwtUtil(@Value("${jwt.secret:quiz-platform-default-dev-secret-key-change-in-prod}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<Claims> validateAndExtract(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public String generateToken(Long userId, String role, long expiryMillis) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMillis))
                .signWith(signingKey)
                .compact();
    }

    public Optional<Long> extractUserId(String token) {
        return validateAndExtract(token)
                .map(claims -> Long.parseLong(claims.getSubject()));
    }
}
