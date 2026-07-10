package uz.quizplatform.telegramservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Value("${services.user-service.base-url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${services.quiz-service.base-url:http://localhost:8083}")
    private String quizServiceUrl;

    @Value("${services.question-service.base-url:http://localhost:8084}")
    private String questionServiceUrl;

    @Value("${services.import-service.base-url:http://localhost:8087}")
    private String importServiceUrl;

    @Bean
    public RestClient userServiceClientInstance() {
        return RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    public RestClient quizServiceClientInstance() {
        return RestClient.builder()
                .baseUrl(quizServiceUrl)
                .build();
    }

    @Bean
    public RestClient questionServiceClientInstance() {
        return RestClient.builder()
                .baseUrl(questionServiceUrl)
                .build();
    }

    @Bean
    public RestClient importServiceClientInstance() {
        return RestClient.builder()
                .baseUrl(importServiceUrl)
                .build();
    }
}
