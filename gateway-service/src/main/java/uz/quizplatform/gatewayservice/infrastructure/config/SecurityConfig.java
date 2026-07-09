package uz.quizplatform.gatewayservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/v1/auth/**").permitAll()
                .anyExchange().authenticated()
            )
            // JWT validation is done in individual microservices via common-security.
            // Here we just pass the token down, but could do edge validation if preferred.
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
            
        return http.build();
    }
}
