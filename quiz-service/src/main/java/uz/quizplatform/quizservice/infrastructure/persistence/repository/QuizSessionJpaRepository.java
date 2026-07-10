package uz.quizplatform.quizservice.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.quizplatform.quizservice.domain.valueobject.QuizStatus;
import uz.quizplatform.quizservice.infrastructure.persistence.entity.QuizSessionJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizSessionJpaRepository extends JpaRepository<QuizSessionJpaEntity, UUID> {

    Optional<QuizSessionJpaEntity> findByUserIdAndStatus(Long userId, QuizStatus status);
    Optional<QuizSessionJpaEntity> findByCurrentPollIdAndStatus(String currentPollId, QuizStatus status);

    @Query("SELECT s FROM QuizSessionJpaEntity s WHERE s.status = 'ACTIVE'")
    List<QuizSessionJpaEntity> findAllActive();
}
