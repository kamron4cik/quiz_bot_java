package uz.quizplatform.questionimportservice.application.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.quizplatform.questionimportservice.domain.entity.ParsedQuestion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class TxtParser implements DocumentParser {

    @Override
    public boolean supports(String format) {
        return "txt".equalsIgnoreCase(format) || "text/plain".equalsIgnoreCase(format);
    }

    @Override
    public List<ParsedQuestion> parse(InputStream inputStream, UUID jobId) {
        List<ParsedQuestion> questions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            ParsedQuestion.ParsedQuestionBuilder currentQuestion = null;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String text = line.trim();
                if (text.isEmpty()) continue;
                
                if (text.startsWith("Q:") || text.startsWith("Savol:")) {
                    if (currentQuestion != null) {
                        questions.add(buildAndValidate(currentQuestion));
                    }
                    currentQuestion = ParsedQuestion.builder()
                            .id(UUID.randomUUID())
                            .jobId(jobId)
                            .sourceLineNumber(lineNumber)
                            .questionText(text.substring(text.indexOf(":") + 1).trim());
                } else if (currentQuestion != null) {
                    if (text.startsWith("A)")) currentQuestion.optionA(text.substring(2).trim());
                    else if (text.startsWith("B)")) currentQuestion.optionB(text.substring(2).trim());
                    else if (text.startsWith("C)")) currentQuestion.optionC(text.substring(2).trim());
                    else if (text.startsWith("D)")) currentQuestion.optionD(text.substring(2).trim());
                    else if (text.startsWith("E:") || text.startsWith("Javob:")) {
                        currentQuestion.explanation(text.substring(text.indexOf(":") + 1).trim());
                    } else {
                        if (currentQuestion.build().getOptionD() == null && currentQuestion.build().getQuestionText() != null) {
                           currentQuestion.questionText(currentQuestion.build().getQuestionText() + "\n" + text);
                        }
                    }
                }
            }
            
            if (currentQuestion != null) {
                questions.add(buildAndValidate(currentQuestion));
            }
            
        } catch (Exception e) {
            log.error("Failed to parse TXT file", e);
            throw new RuntimeException("TXT parsing error: " + e.getMessage(), e);
        }
        
        return questions;
    }

    private ParsedQuestion buildAndValidate(ParsedQuestion.ParsedQuestionBuilder builder) {
        ParsedQuestion q = builder.build();
        if (q.getQuestionText() == null || q.getOptionA() == null || q.getOptionB() == null || q.getOptionC() == null || q.getOptionD() == null) {
            q.markInvalid("Missing required options (A, B, C, D)");
        }
        return q;
    }
}
