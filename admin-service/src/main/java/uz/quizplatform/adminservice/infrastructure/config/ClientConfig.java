package uz.quizplatform.adminservice.infrastructure.config;

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

    @Bean
    public RestClient userServiceClient() {
        return RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    public RestClient quizServiceClient() {
        return RestClient.builder()
                .baseUrl(quizServiceUrl)
                .build();
    }

    @Bean
    public RestClient questionServiceClient() {
        return RestClient.builder()
                .baseUrl(questionServiceUrl)
                .build();
    }
}
