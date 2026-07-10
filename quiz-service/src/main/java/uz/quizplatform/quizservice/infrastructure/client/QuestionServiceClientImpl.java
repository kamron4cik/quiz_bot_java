package uz.quizplatform.quizservice.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceClientImpl implements QuestionServiceClient {

    private final RestClient questionServiceClientInstance;

    @Override
    public List<UUID> getQuestionIdsByCategory(UUID categoryId) {
        log.debug("Calling question-service to get question IDs for category: {}", categoryId);
        try {
            return questionServiceClientInstance.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/questions")
                            .queryParam("categoryId", categoryId)
                            .queryParam("full", false)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UUID>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch question IDs for category {}: {}", categoryId, e.getMessage());
            throw new RuntimeException("Question service communication failure", e);
        }
    }

    @Override
    public QuestionDto getQuestionById(UUID questionId) {
        log.debug("Calling question-service to get question details for: {}", questionId);
        try {
            return questionServiceClientInstance.get()
                    .uri("/api/v1/questions/{id}", questionId)
                    .retrieve()
                    .body(QuestionDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch question details for {}: {}", questionId, e.getMessage());
            throw new RuntimeException("Question service communication failure", e);
        }
    }
}
