package uz.quizplatform.notificationservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Value("${services.quiz-service.base-url:http://localhost:8083}")
    private String quizServiceUrl;

    @Bean
    public RestClient quizServiceClientInstance() {
        return RestClient.builder()
                .baseUrl(quizServiceUrl)
                .build();
    }
}
