package uz.quizplatform.quizservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.infrastructure.persistence.entity.UserQuizStatsJpaEntity;
import uz.quizplatform.quizservice.infrastructure.persistence.repository.UserQuizStatsJpaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQuizStatsService {

    private final UserQuizStatsJpaRepository statsRepository;

    @Transactional
    public void updateStats(QuizSession session) {
        log.info("Updating stats for user {} based on session {}", session.getUserId(), session.getId());
        
        UserQuizStatsJpaEntity stats = statsRepository.findByUserId(session.getUserId())
                .orElseGet(() -> UserQuizStatsJpaEntity.builder()
                        .userId(session.getUserId())
                        .universityId(session.getUniversityId())
                        .testsCompleted(0)
                        .questionsSolved(0)
                        .totalCorrect(0)
                        .averageScore(BigDecimal.ZERO)
                        .bestScore(BigDecimal.ZERO)
                        .totalStudyTimeSec(0)
                        .build());

        int prevTotalSolved = stats.getQuestionsSolved();
        int newSolved = session.getTotalCorrect() + session.getTotalWrong();
        int totalSolved = prevTotalSolved + newSolved;

        int totalCorrect = stats.getTotalCorrect() + session.getTotalCorrect();
        
        BigDecimal currentScore = BigDecimal.valueOf(session.getScore());
        BigDecimal bestScore = stats.getBestScore().max(currentScore);

        BigDecimal averageScore = totalSolved > 0 
                ? BigDecimal.valueOf(totalCorrect).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalSolved), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        stats.setUniversityId(session.getUniversityId());
        stats.setTestsCompleted(stats.getTestsCompleted() + 1);
        stats.setQuestionsSolved(totalSolved);
        stats.setTotalCorrect(totalCorrect);
        stats.setAverageScore(averageScore);
        stats.setBestScore(bestScore);
        stats.setTotalStudyTimeSec(stats.getTotalStudyTimeSec() + session.getDurationSeconds());
        stats.setUpdatedAt(Instant.now());

        statsRepository.save(stats);
        log.info("Stats updated for user {}: avgScore={}, bestScore={}", stats.getUserId(), stats.getAverageScore(), stats.getBestScore());
    }

    @Transactional(readOnly = true)
    public Optional<UserQuizStatsJpaEntity> getUserStats(Long userId) {
        return statsRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getUserRank(Long userId) {
        return statsRepository.getRankForUser(userId);
    }

    @Transactional(readOnly = true)
    public long getUserRankInUniversity(Long userId, UUID universityId) {
        return statsRepository.getRankForUserInUniversity(userId, universityId);
    }

    @Transactional(readOnly = true)
    public List<UserQuizStatsJpaEntity> getGlobalLeaderboard(int limit) {
        return statsRepository.findTopByOrderByAverageScoreDesc(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<UserQuizStatsJpaEntity> getUniversityLeaderboard(UUID universityId, int limit) {
        return statsRepository.findByUniversityIdOrderByAverageScoreDesc(universityId, PageRequest.of(0, limit));
    }
}
