package uz.quizplatform.userservice.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.quizplatform.common.domain.exception.ResourceNotFoundException;
import uz.quizplatform.userservice.application.dto.request.UpdateUserProfileRequest;
import uz.quizplatform.userservice.application.dto.response.UserResponse;
import uz.quizplatform.userservice.domain.entity.Admin;
import uz.quizplatform.userservice.domain.entity.University;
import uz.quizplatform.userservice.domain.entity.User;
import uz.quizplatform.userservice.domain.repository.AdminRepository;
import uz.quizplatform.userservice.domain.repository.UniversityRepository;
import uz.quizplatform.userservice.domain.repository.UserRepository;
import uz.quizplatform.userservice.domain.valueobject.StudyMethod;
import uz.quizplatform.userservice.domain.valueobject.TestType;

import java.util.Optional;

/**
 * Use Case: Update a user's academic profile.
 *
 * Handles partial updates: any field can be set independently.
 * The telegram-service calls this for each step of the 5-step wizard:
 *   Step 1: universityId
 *   Step 2: major
 *   Step 3: grade
 *   Step 4: studyMethod
 *   Step 5: testType
 *
 * Also supports profile reset (all fields set to null).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final UniversityRepository universityRepository;

    public UserResponse execute(Long userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Apply partial updates — only update fields that are present in the request
        var universityId = request.getUniversityId() != null ? request.getUniversityId() : user.getUniversityId();
        var major = request.getMajor() != null ? request.getMajor() : user.getMajor();
        var grade = request.getGrade() != null ? request.getGrade() : user.getGrade();
        var studyMethod = request.getStudyMethod() != null
                ? StudyMethod.fromCode(request.getStudyMethod())
                : user.getStudyMethod();
        var testType = request.getTestType() != null
                ? TestType.fromCode(request.getTestType())
                : user.getTestType();

        user.updateProfile(universityId, major, grade, studyMethod, testType);
        user.updateActivity();
        user = userRepository.save(user);

        log.debug("User profile updated: userId={}, profileComplete={}", userId, user.isProfileComplete());

        Optional<Admin> admin = adminRepository.findByTelegramId(userId);
        Optional<University> university = universityId != null
                ? universityRepository.findById(universityId)
                : Optional.empty();

        return buildResponse(user, admin.orElse(null), university.orElse(null));
    }

    public UserResponse resetProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.resetProfile();
        user.updateActivity();
        user = userRepository.save(user);

        log.info("User profile reset: userId={}", userId);
        return buildResponse(user, null, null);
    }

    private UserResponse buildResponse(User user, Admin admin, University university) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .displayName(user.getDisplayName())
                .universityId(user.getUniversityId())
                .universityName(university != null ? university.getName() : null)
                .major(user.getMajor())
                .grade(user.getGrade())
                .studyMethod(user.getStudyMethod() != null ? user.getStudyMethod().getCode() : null)
                .testType(user.getTestType() != null ? user.getTestType().getCode() : null)
                .hasPaid(user.isHasPaid())
                .profileComplete(user.isProfileComplete())
                .isAdmin(admin != null)
                .totalTests(user.getTotalTests())
                .totalQuestions(user.getTotalQuestions())
                .averageScore(user.getAverageScore())
                .createdAt(user.getCreatedAt())
                .lastActivity(user.getLastActivity())
                .build();
    }
}
