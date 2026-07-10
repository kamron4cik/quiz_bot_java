package uz.quizplatform.telegramservice.infrastructure.state;

public interface UserStateManager {
    void setPendingNewQuiz(Long userId, boolean pending);
    boolean isPendingNewQuiz(Long userId);
    
    void setWizardStep(Long userId, String step);
    String getWizardStep(Long userId);
    void clearWizard(Long userId);
    
    void setProfileData(Long userId, String key, String value);
    String getProfileData(Long userId, String key);
    void clearProfileData(Long userId);
}
