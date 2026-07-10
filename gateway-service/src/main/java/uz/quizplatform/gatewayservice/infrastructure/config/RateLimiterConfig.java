package uz.quizplatform.gatewayservice.infrastructure.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    /**
     * Resolves rate limiting key based on X-User-Id header (for authenticated users)
     * or remote IP address (for public routes/unauthenticated users).
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            String userId = request.getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId);
            }
            return Mono.just("ip:" + Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress());
        };
    }
}
