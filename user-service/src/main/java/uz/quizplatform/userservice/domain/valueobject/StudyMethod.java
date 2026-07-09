package uz.quizplatform.userservice.domain.valueobject;

/**
 * Represents a user's study method (form of education) at their university.
 * Maps from Uzbek: kunduzgi=daytime, kechki=evening, sirtqi=correspondence, masofaviy=distance
 */
public enum StudyMethod {
    KUNDUZGI("kunduzgi", "Kunduzgi (Kunduzgi)"),
    KECHKI("kechki", "Kechki (Kechki)"),
    SIRTQI("sirtqi", "Sirtqi (Sirtqi)"),
    MASOFAVIY("masofaviy", "Masofaviy (Masofaviy)");

    private final String code;
    private final String displayName;

    StudyMethod(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }

    public static StudyMethod fromCode(String code) {
        for (StudyMethod sm : values()) {
            if (sm.code.equalsIgnoreCase(code)) return sm;
        }
        throw new IllegalArgumentException("Unknown study method code: " + code);
    }
}
