package uz.quizplatform.userservice.domain.repository;

import uz.quizplatform.userservice.domain.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Domain port (interface) for User persistence.
 * The infrastructure layer provides the JPA implementation.
 * Domain layer depends only on this interface — never on JPA directly.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long telegramId);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    List<User> findAllPaginated(int page, int size);

    long count();

    /** For analytics: count users active in the last N minutes */
    List<User> findActiveAfter(Instant since);

    /** For analytics scoped to a university */
    List<User> findByUniversityId(java.util.UUID universityId);

    long countByUniversityId(java.util.UUID universityId);

    long countPaidUsers(java.util.UUID universityId);
}
