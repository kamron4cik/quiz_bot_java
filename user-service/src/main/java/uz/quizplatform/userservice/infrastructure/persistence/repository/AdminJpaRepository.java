package uz.quizplatform.userservice.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.quizplatform.userservice.infrastructure.persistence.entity.AdminJpaEntity;

import java.util.Optional;

public interface AdminJpaRepository extends JpaRepository<AdminJpaEntity, Long> {
    Optional<AdminJpaEntity> findByTelegramId(Long telegramId);
}
