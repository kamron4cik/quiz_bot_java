package uz.quizplatform.questionimportservice.application.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import uz.quizplatform.questionimportservice.domain.entity.ParsedQuestion;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class DocxParser implements DocumentParser {

    @Override
    public boolean supports(String format) {
        return "docx".equalsIgnoreCase(format) || "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(format);
    }

    @Override
    public List<ParsedQuestion> parse(InputStream inputStream, UUID jobId) {
        List<ParsedQuestion> questions = new ArrayList<>();
        
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            ParsedQuestion.ParsedQuestionBuilder currentQuestion = null;
            int lineNumber = 0;
            
            for (XWPFParagraph paragraph : paragraphs) {
                lineNumber++;
                String text = paragraph.getText().trim();
                if (text.isEmpty()) continue;
                
                // Simple heuristic parsing logic.
                // In production, this needs to be much more robust (e.g. regex for A), B), C), D)).
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
                        // Append to the last seen field (multi-line support)
                        if (currentQuestion.build().getOptionD() == null && currentQuestion.build().getQuestionText() != null) {
                           // For simplicity, we just append to question text
                           currentQuestion.questionText(currentQuestion.build().getQuestionText() + "\n" + text);
                        }
                    }
                }
            }
            
            if (currentQuestion != null) {
                questions.add(buildAndValidate(currentQuestion));
            }
            
        } catch (Exception e) {
            log.error("Failed to parse DOCX file", e);
            throw new RuntimeException("DOCX parsing error: " + e.getMessage(), e);
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
