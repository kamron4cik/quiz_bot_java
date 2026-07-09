package uz.quizplatform.questionimportservice.domain.repository;

import uz.quizplatform.questionimportservice.domain.entity.ParsedQuestion;

import java.util.List;
import java.util.UUID;

public interface ParsedQuestionRepository {
    ParsedQuestion save(ParsedQuestion question);
    void saveAll(List<ParsedQuestion> questions);
    List<ParsedQuestion> findByJobId(UUID jobId);
    void deleteByJobId(UUID jobId);
}
