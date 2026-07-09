package uz.quizplatform.userservice.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.quizplatform.userservice.domain.entity.User;
import uz.quizplatform.userservice.domain.repository.UserRepository;
import uz.quizplatform.userservice.infrastructure.persistence.mapper.UserEntityMapper;
import uz.quizplatform.userservice.infrastructure.persistence.repository.UserJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements the domain UserRepository port using Spring Data JPA.
 * This is the only place in the codebase that knows about JPA/Hibernate.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper mapper;

    @Override
    public User save(User user) {
        var jpaEntity = mapper.toJpaEntity(user);
        var saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long telegramId) {
        return jpaRepository.findById(telegramId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsernameIgnoreCase(username).map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<User> findAllPaginated(int page, int size) {
        int offset = page * size;
        return jpaRepository.findAllPaginated(offset, size).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<User> findActiveAfter(Instant since) {
        return jpaRepository.findByLastActivityAfter(since).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<User> findByUniversityId(UUID universityId) {
        return jpaRepository.findByUniversityId(universityId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByUniversityId(UUID universityId) {
        return jpaRepository.countByUniversityId(universityId);
    }

    @Override
    public long countPaidUsers(UUID universityId) {
        return universityId != null
                ? jpaRepository.countByUniversityIdAndHasPaidTrue(universityId)
                : jpaRepository.findAll().stream().filter(u -> u.isHasPaid()).count();
    }
}
