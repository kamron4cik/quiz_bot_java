package uz.quizplatform.quizservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import uz.quizplatform.common.domain.event.QuizSessionFinishedEvent;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.CategoryRepository;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizEventPublisherImpl implements QuizEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final CategoryRepository categoryRepository;

    private static final String EXCHANGE = "quiz.events";
    private static final String ROUTING_KEY = "quiz.session.finished";

    @Override
    public void publishSessionFinished(QuizSession session) {
        String categoryName = categoryRepository.findById(session.getCategoryId())
                .map(CategoryRepository.Category::getName)
                .orElse("Kategoriya");

        String correlationId = UUID.randomUUID().toString();
        int scorePercentage = (int) Math.round(session.getScore());

        QuizSessionFinishedEvent event = new QuizSessionFinishedEvent(
                correlationId,
                session.getId().toString(),
                session.getUserId(),
                session.getCategoryId(),
                categoryName,
                session.getQuestionCount(),
                session.getTotalCorrect(),
                session.getTotalWrong(),
                scorePercentage,
                session.getDurationSeconds(),
                session.getStatus().name()
        );

        log.info("Publishing QuizSessionFinishedEvent for session {}, user {}, status={}",
                session.getId(), session.getUserId(), session.getStatus());
        
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}
