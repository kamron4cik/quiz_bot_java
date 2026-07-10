package uz.quizplatform.quizservice.application.usecase.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.quizplatform.common.domain.exception.BusinessRuleViolationException;
import uz.quizplatform.quizservice.application.dto.request.StartQuizRequest;
import uz.quizplatform.quizservice.application.dto.response.QuizSessionResponse;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.CategoryRepository;
import uz.quizplatform.quizservice.domain.repository.QuizSessionRepository;
import uz.quizplatform.quizservice.infrastructure.cache.QuizSessionCache;
import uz.quizplatform.quizservice.infrastructure.client.QuestionServiceClient;
import uz.quizplatform.quizservice.infrastructure.client.UserServiceClient;
import uz.quizplatform.quizservice.infrastructure.messaging.QuizEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartQuizSessionUseCaseTest {

    @Mock
    private QuizSessionRepository sessionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private QuestionServiceClient questionServiceClient;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private QuizSessionCache sessionCache;
    @Mock
    private QuizEventPublisher eventPublisher;

    @InjectMocks
    private StartQuizSessionUseCase startQuizSessionUseCase;

    private Long userId;
    private UUID categoryId;
    private UUID universityId;
    private UserServiceClient.User mockUser;
    private CategoryRepository.Category mockCategory;
    private List<UUID> mockQuestionIds;

    @BeforeEach
    void setUp() {
        userId = 12345L;
        categoryId = UUID.randomUUID();
        universityId = UUID.randomUUID();

        mockUser = mock(UserServiceClient.User.class);
        lenient().when(mockUser.getUniversityId()).thenReturn(universityId);

        mockCategory = mock(CategoryRepository.Category.class);
        lenient().when(mockCategory.getName()).thenReturn("Test Category");
        lenient().when(mockCategory.isActive()).thenReturn(true);

        mockQuestionIds = List.of(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
    }

    @Test
    void execute_UserHasNotPaid_ThrowsPaymentRequired() {
        when(userServiceClient.getUser(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.isAdmin()).thenReturn(false);
        when(mockUser.isHasPaid()).thenReturn(false);

        StartQuizRequest request = new StartQuizRequest();
        request.setUserId(userId);
        request.setCategoryId(categoryId);

        assertThrows(BusinessRuleViolationException.class, () -> startQuizSessionUseCase.execute(request));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void execute_ProfileIncomplete_ThrowsProfileIncomplete() {
        when(userServiceClient.getUser(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.isAdmin()).thenReturn(false);
        when(mockUser.isHasPaid()).thenReturn(true);
        when(mockUser.isProfileComplete()).thenReturn(false);

        StartQuizRequest request = new StartQuizRequest();
        request.setUserId(userId);
        request.setCategoryId(categoryId);

        assertThrows(BusinessRuleViolationException.class, () -> startQuizSessionUseCase.execute(request));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void execute_Success() {
        when(userServiceClient.getUser(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.isAdmin()).thenReturn(true); // admins bypass checks
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
        when(questionServiceClient.getQuestionIdsByCategory(categoryId)).thenReturn(mockQuestionIds);
        when(sessionRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());

        when(sessionRepository.save(any(QuizSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StartQuizRequest request = new StartQuizRequest();
        request.setUserId(userId);
        request.setCategoryId(categoryId);
        request.setMode("RANDOM");
        request.setQuestionCount(5);
        request.setTimePerQuestionSeconds(30);

        QuizSessionResponse response = startQuizSessionUseCase.execute(request);

        assertNotNull(response);
        assertEquals("Test Category", response.getCategoryName());
        verify(sessionRepository, times(1)).save(any(QuizSession.class));
        verify(sessionCache, times(1)).put(eq(userId), any(QuizSession.class));
    }
}
