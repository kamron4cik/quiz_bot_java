package uz.quizplatform.quizservice.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.quizplatform.quizservice.infrastructure.persistence.entity.UserQuizStatsJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserQuizStatsJpaRepository extends JpaRepository<UserQuizStatsJpaEntity, Long> {

    Optional<UserQuizStatsJpaEntity> findByUserId(Long userId);

    @Query("SELECT s FROM UserQuizStatsJpaEntity s ORDER BY s.averageScore DESC")
    List<UserQuizStatsJpaEntity> findTopByOrderByAverageScoreDesc(Pageable pageable);

    @Query("SELECT s FROM UserQuizStatsJpaEntity s WHERE s.universityId = :universityId ORDER BY s.averageScore DESC")
    List<UserQuizStatsJpaEntity> findByUniversityIdOrderByAverageScoreDesc(UUID universityId, Pageable pageable);

    @Query("SELECT COUNT(s) + 1 FROM UserQuizStatsJpaEntity s WHERE s.averageScore > (SELECT u.averageScore FROM UserQuizStatsJpaEntity u WHERE u.userId = :userId)")
    long getRankForUser(Long userId);

    @Query("SELECT COUNT(s) + 1 FROM UserQuizStatsJpaEntity s WHERE s.universityId = :universityId AND s.averageScore > (SELECT u.averageScore FROM UserQuizStatsJpaEntity u WHERE u.userId = :userId)")
    long getRankForUserInUniversity(Long userId, UUID universityId);
}
