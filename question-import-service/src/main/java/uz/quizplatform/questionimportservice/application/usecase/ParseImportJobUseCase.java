package uz.quizplatform.questionimportservice.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.questionimportservice.application.parser.DocumentParser;
import uz.quizplatform.questionimportservice.domain.entity.ImportJob;
import uz.quizplatform.questionimportservice.domain.entity.ParsedQuestion;
import uz.quizplatform.questionimportservice.domain.repository.ImportJobRepository;
import uz.quizplatform.questionimportservice.domain.repository.ParsedQuestionRepository;
import uz.quizplatform.questionimportservice.infrastructure.storage.MinioStorageService;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParseImportJobUseCase {

    private final ImportJobRepository importJobRepository;
    private final ParsedQuestionRepository parsedQuestionRepository;
    private final MinioStorageService storageService;
    private final List<DocumentParser> parsers;

    @Async("importTaskExecutor")
    @Transactional
    public void execute(UUID jobId) {
        ImportJob job = importJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        try {
            job.markAsParsing();
            importJobRepository.save(job);

            DocumentParser selectedParser = parsers.stream()
                    .filter(p -> p.supports(job.getFileFormat()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported file format: " + job.getFileFormat()));

            InputStream fileStream = storageService.getFile(job.getFilePath());
            List<ParsedQuestion> questions = selectedParser.parse(fileStream, jobId);

            // Basic validation metrics
            int valid = 0, invalid = 0, duplicate = 0;
            for (ParsedQuestion q : questions) {
                if (!q.isValid()) invalid++;
                else if (q.isDuplicate()) duplicate++;
                else valid++;
            }

            parsedQuestionRepository.saveAll(questions);
            job.markAsPreviewReady(questions.size(), valid, duplicate, invalid);
            importJobRepository.save(job);
            
            log.info("Successfully parsed job {}: {} total, {} valid", jobId, questions.size(), valid);

        } catch (Exception e) {
            log.error("Failed to process import job " + jobId, e);
            job.markAsFailed(e.getMessage());
            importJobRepository.save(job);
        }
    }
}
