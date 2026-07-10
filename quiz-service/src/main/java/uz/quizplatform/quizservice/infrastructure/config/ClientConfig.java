package uz.quizplatform.quizservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Value("${services.user-service.base-url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${services.question-service.base-url:http://localhost:8084}")
    private String questionServiceUrl;

    @Bean
    public RestClient userServiceClientInstance() {
        return RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    public RestClient questionServiceClientInstance() {
        return RestClient.builder()
                .baseUrl(questionServiceUrl)
                .build();
    }
}
