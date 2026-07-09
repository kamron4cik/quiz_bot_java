package uz.quizplatform.questionimportservice.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.quizplatform.questionimportservice.domain.entity.ParsedQuestion;
import uz.quizplatform.questionimportservice.domain.repository.ParsedQuestionRepository;
import uz.quizplatform.questionimportservice.infrastructure.persistence.mapper.ParsedQuestionEntityMapper;
import uz.quizplatform.questionimportservice.infrastructure.persistence.repository.ParsedQuestionJpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ParsedQuestionRepositoryAdapter implements ParsedQuestionRepository {

    private final ParsedQuestionJpaRepository jpaRepository;
    private final ParsedQuestionEntityMapper mapper;

    @Override
    public ParsedQuestion save(ParsedQuestion question) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpaEntity(question)));
    }

    @Override
    public void saveAll(List<ParsedQuestion> questions) {
        jpaRepository.saveAll(questions.stream().map(mapper::toJpaEntity).collect(Collectors.toList()));
    }

    @Override
    public List<ParsedQuestion> findByJobId(UUID jobId) {
        return jpaRepository.findByJobId(jobId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteByJobId(UUID jobId) {
        jpaRepository.deleteByJobId(jobId);
    }
}
