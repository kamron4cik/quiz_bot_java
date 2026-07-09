package uz.quizplatform.questionimportservice.application.parser;

import uz.quizplatform.questionimportservice.domain.entity.ParsedQuestion;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface DocumentParser {
    boolean supports(String format);
    List<ParsedQuestion> parse(InputStream inputStream, UUID jobId);
}
