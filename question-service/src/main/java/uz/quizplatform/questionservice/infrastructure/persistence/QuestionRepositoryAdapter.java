package uz.quizplatform.questionservice.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.quizplatform.questionservice.domain.entity.Question;
import uz.quizplatform.questionservice.domain.repository.QuestionRepository;
import uz.quizplatform.questionservice.infrastructure.persistence.mapper.QuestionEntityMapper;
import uz.quizplatform.questionservice.infrastructure.persistence.repository.QuestionJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class QuestionRepositoryAdapter implements QuestionRepository {

    private final QuestionJpaRepository jpaRepository;
    private final QuestionEntityMapper mapper;

    @Override
    public Question save(Question question) {
        var entity = mapper.toEntity(question);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Question> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<UUID> findIdsByCategoryId(UUID categoryId) {
        return jpaRepository.findIdsByCategoryIdAndActiveTrue(categoryId);
    }

    @Override
    public List<Question> findByCategoryId(UUID categoryId) {
        return jpaRepository.findByCategoryIdAndActiveTrue(categoryId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByCategoryId(UUID categoryId) {
        return jpaRepository.countByCategoryIdAndActiveTrue(categoryId);
    }
}
