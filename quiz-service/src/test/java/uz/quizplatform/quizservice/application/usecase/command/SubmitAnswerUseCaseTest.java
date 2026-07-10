package uz.quizplatform.quizservice.application.usecase.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.quizplatform.quizservice.domain.entity.QuizSession;
import uz.quizplatform.quizservice.domain.repository.QuizSessionRepository;
import uz.quizplatform.quizservice.infrastructure.cache.QuizSessionCache;
import uz.quizplatform.quizservice.infrastructure.client.NotificationServiceClient;
import uz.quizplatform.quizservice.infrastructure.messaging.QuizEventPublisher;
import uz.quizplatform.quizservice.application.service.UserQuizStatsService;
import uz.quizplatform.quizservice.application.service.LeaderboardService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitAnswerUseCaseTest {

    @Mock
    private QuizSessionRepository sessionRepository;
    @Mock
    private QuizSessionCache sessionCache;
    @Mock
    private QuizEventPublisher eventPublisher;
    @Mock
    private NotificationServiceClient notificationClient;
    @Mock
    private UserQuizStatsService statsService;
    @Mock
    private LeaderboardService leaderboardService;

    @InjectMocks
    private SubmitAnswerUseCase submitAnswerUseCase;

    private String pollId;
    private Long userId;
    private QuizSession mockSession;

    @BeforeEach
    void setUp() {
        pollId = "telegram_poll_123";
        userId = 12345L;

        mockSession = mock(QuizSession.class);
        when(mockSession.getUserId()).thenReturn(userId);
    }

    @Test
    void execute_SessionUserMismatch_ThrowsIllegalArgumentException() {
        when(sessionRepository.findActiveByPollId(pollId)).thenReturn(Optional.of(mockSession));
        
        assertThrows(IllegalArgumentException.class, () -> 
                submitAnswerUseCase.execute(pollId, 99999L, 0));
        
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void execute_CorrectAnswer_AdvancesSession() {
        when(sessionRepository.findActiveByPollId(pollId)).thenReturn(Optional.of(mockSession));
        when(mockSession.getCurrentCorrectOptionIndex()).thenReturn(1);
        when(mockSession.getCurrentShuffledOptions()).thenReturn(List.of("Option A", "Option B", "Option C"));
        when(mockSession.hasMoreQuestions()).thenReturn(true);
        when(mockSession.getId()).thenReturn(UUID.randomUUID());

        var response = submitAnswerUseCase.execute(pollId, userId, 1);

        assertNotNull(response);
        assertFalse(response.isSessionFinished());
        verify(mockSession, times(1)).recordAnswer("Option B", true);
        verify(sessionRepository, times(1)).save(mockSession);
        verify(sessionCache, times(1)).put(userId, mockSession);
        verify(notificationClient, times(1)).sendNextQuestion(mockSession);
    }
}
