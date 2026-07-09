package uz.quizplatform.userservice.domain.repository;

import uz.quizplatform.userservice.domain.entity.University;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Domain port for University persistence. */
public interface UniversityRepository {
    University save(University university);
    Optional<University> findById(UUID id);
    List<University> findAll();
    boolean existsByName(String name);
}
