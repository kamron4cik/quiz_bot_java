package uz.quizplatform.userservice.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.quizplatform.userservice.domain.entity.Admin;
import uz.quizplatform.userservice.domain.entity.University;
import uz.quizplatform.userservice.domain.repository.AdminRepository;
import uz.quizplatform.userservice.domain.repository.UniversityRepository;
import uz.quizplatform.userservice.infrastructure.persistence.mapper.UniversityEntityMapper;
import uz.quizplatform.userservice.infrastructure.persistence.repository.AdminJpaRepository;
import uz.quizplatform.userservice.infrastructure.persistence.repository.UniversityJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UniversityRepositoryAdapter implements UniversityRepository, AdminRepository {

    private final UniversityJpaRepository universityJpaRepository;
    private final AdminJpaRepository adminJpaRepository;
    private final UniversityEntityMapper mapper;

    // UniversityRepository

    @Override
    public University save(University university) {
        return mapper.toDomain(universityJpaRepository.save(mapper.toJpaEntity(university)));
    }

    @Override
    public Optional<University> findById(UUID id) {
        return universityJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<University> findAll() {
        return universityJpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return universityJpaRepository.existsByNameIgnoreCase(name);
    }

    // AdminRepository

    @Override
    public Optional<Admin> findByTelegramId(Long telegramId) {
        return adminJpaRepository.findByTelegramId(telegramId).map(mapper::toDomain);
    }

    @Override
    public Admin save(Admin admin) {
        return mapper.toDomain(adminJpaRepository.save(mapper.toJpaEntity(admin)));
    }

    @Override
    public boolean existsByTelegramId(Long telegramId) {
        return adminJpaRepository.existsById(telegramId);
    }
}
