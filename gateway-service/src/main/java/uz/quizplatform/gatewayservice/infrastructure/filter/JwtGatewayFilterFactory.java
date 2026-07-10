package uz.quizplatform.gatewayservice.infrastructure.filter;

import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import uz.quizplatform.common.security.JwtUtil;

import java.util.List;

/**
 * Gateway filter for JWT validation and downstream header propagation.
 *
 * Configurable with 'requiredRole' (e.g. ADMIN) or empty.
 * If token is valid, it injects X-User-Id and X-User-Role downstream.
 */
@Slf4j
@Component
public class JwtGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public JwtGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("requiredRole");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            var claimsOpt = jwtUtil.validateAndExtract(token);

            if (claimsOpt.isEmpty()) {
                return onError(exchange, "Invalid/Expired Token", HttpStatus.UNAUTHORIZED);
            }

            Claims claims = claimsOpt.get();
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // Role check if configured
            if (config.getRequiredRole() != null && !config.getRequiredRole().isEmpty()) {
                if (!config.getRequiredRole().equalsIgnoreCase(role)) {
                    log.warn("Access denied for user {} (role={}). Required role: {}", userId, role, config.getRequiredRole());
                    return onError(exchange, "Forbidden", HttpStatus.FORBIDDEN);
                }
            }

            // Propagate headers downstream
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role != null ? role : "USER")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        log.warn("Gateway JWT auth failed: {} -> returning {}", err, status);
        return response.setComplete();
    }

    @Data
    public static class Config {
        private String requiredRole;
    }
}
