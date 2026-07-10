package uz.quizplatform.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Default permissive security configuration for internal microservices.
 *
 * Internal services (question-service, quiz-service, etc.) are accessed only
 * through the API Gateway, which performs JWT validation. Services behind the
 * gateway do NOT need to re-validate tokens — they trust the gateway's
 * X-User-Id header.
 *
 * Production deployments should add network-level isolation (e.g., Kubernetes
 * NetworkPolicy) to ensure services are not directly accessible from outside.
 *
 * Services that need custom security (e.g., user-service for auth endpoints)
 * should define their own SecurityFilterChain bean, which will override this one
 * due to ConditionalOnMissingBean.
 */
@Configuration
public class DefaultSecurityConfig {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain defaultPermitAll(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
