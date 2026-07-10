package uz.quizplatform.questionservice.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.quizplatform.questionservice.domain.entity.Category;
import uz.quizplatform.questionservice.domain.repository.CategoryRepository;
import uz.quizplatform.questionservice.infrastructure.persistence.mapper.CategoryEntityMapper;
import uz.quizplatform.questionservice.infrastructure.persistence.repository.CategoryJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;
    private final CategoryEntityMapper mapper;

    @Override
    public Category save(Category category) {
        var entity = mapper.toEntity(category);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Category> findByUniversityId(UUID universityId) {
        return jpaRepository.findByUniversityIdAndActiveTrue(universityId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Category> findAllActive() {
        return jpaRepository.findByActiveTrue()
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByUniversityIdAndName(UUID universityId, String name) {
        return jpaRepository.existsByUniversityIdAndName(universityId, name);
    }
}
