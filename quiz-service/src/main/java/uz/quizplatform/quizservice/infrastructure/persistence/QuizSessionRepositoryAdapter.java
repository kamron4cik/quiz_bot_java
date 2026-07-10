package uz.quizplatform.quizservice.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.QuizSessionRepository;
import uz.quizplatform.quizservice.infrastructure.persistence.mapper.QuizSessionEntityMapper;
import uz.quizplatform.quizservice.infrastructure.persistence.repository.QuizSessionJpaRepository;
import uz.quizplatform.quizservice.domain.valueobject.QuizStatus;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuizSessionRepositoryAdapter implements QuizSessionRepository {

    private final QuizSessionJpaRepository jpaRepository;
    private final QuizSessionEntityMapper mapper;

    @Override
    public Optional<QuizSession> findActiveByUserId(Long userId) {
        return jpaRepository.findByUserIdAndStatus(userId, QuizStatus.ACTIVE)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<QuizSession> findPausedByUserId(Long userId) {
        return jpaRepository.findByUserIdAndStatus(userId, QuizStatus.PAUSED)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<QuizSession> findActiveByPollId(String pollId) {
        return jpaRepository.findByCurrentPollIdAndStatus(pollId, QuizStatus.ACTIVE)
                .map(mapper::toDomain);
    }

    @Override
    public List<QuizSession> findAllActive() {
        return jpaRepository.findAllActive().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public QuizSession save(QuizSession session) {
        var entity = mapper.toEntity(session);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
