package uz.quizplatform.userservice.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.quizplatform.userservice.infrastructure.persistence.entity.UserJpaEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByUsernameIgnoreCase(String username);

    List<UserJpaEntity> findByLastActivityAfter(Instant since);

    List<UserJpaEntity> findByUniversityId(UUID universityId);

    long countByUniversityId(UUID universityId);

    long countByUniversityIdAndHasPaidTrue(UUID universityId);

    @Query("""
        SELECT u FROM UserJpaEntity u
        ORDER BY u.lastActivity DESC
        LIMIT :size OFFSET :offset
        """)
    List<UserJpaEntity> findAllPaginated(@Param("offset") int offset, @Param("size") int size);
}
