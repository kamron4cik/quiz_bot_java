package uz.quizplatform.userservice.domain.repository;

import uz.quizplatform.userservice.domain.entity.Admin;

import java.util.Optional;

/** Domain port for Admin persistence. */
public interface AdminRepository {
    Optional<Admin> findByTelegramId(Long telegramId);
    Admin save(Admin admin);
    boolean existsByTelegramId(Long telegramId);
}
