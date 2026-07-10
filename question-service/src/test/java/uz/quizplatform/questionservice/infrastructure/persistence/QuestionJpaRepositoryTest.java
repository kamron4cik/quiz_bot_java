package uz.quizplatform.questionservice.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uz.quizplatform.questionservice.AbstractIntegrationTest;
import uz.quizplatform.questionservice.domain.valueobject.Difficulty;
import uz.quizplatform.questionservice.infrastructure.persistence.entity.CategoryJpaEntity;
import uz.quizplatform.questionservice.infrastructure.persistence.entity.QuestionJpaEntity;
import uz.quizplatform.questionservice.infrastructure.persistence.repository.CategoryJpaRepository;
import uz.quizplatform.questionservice.infrastructure.persistence.repository.QuestionJpaRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuestionJpaRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private QuestionJpaRepository questionJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Test
    void testSaveAndFindIdsByCategoryId() {
        UUID universityId = UUID.randomUUID();
        
        CategoryJpaEntity category = CategoryJpaEntity.builder()
                .universityId(universityId)
                .name("Math Integration")
                .description("Calculus quizzes")
                .active(true)
                .build();
        CategoryJpaEntity savedCategory = categoryJpaRepository.save(category);
        assertNotNull(savedCategory.getId());

        QuestionJpaEntity question = QuestionJpaEntity.builder()
                .categoryId(savedCategory.getId())
                .text("What is integral of 2x?")
                .optionA("x^2")
                .optionB("2")
                .optionC("x")
                .optionD("x^3")
                .correctAnswer(0)
                .difficulty(Difficulty.EASY)
                .active(true)
                .build();
        QuestionJpaEntity savedQuestion = questionJpaRepository.save(question);
        assertNotNull(savedQuestion.getId());

        List<UUID> activeIds = questionJpaRepository.findIdsByCategoryIdAndActiveTrue(savedCategory.getId());
        assertEquals(1, activeIds.size());
        assertEquals(savedQuestion.getId(), activeIds.get(0));
    }
}
