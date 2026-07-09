package uz.quizplatform.userservice.domain.valueobject;

/**
 * Represents the type of exam a student is studying for.
 * oraliq = midterm exam
 * yakuniy = final exam
 */
public enum TestType {
    ORALIQ("oraliq", "Oraliq test (Midterm)"),
    YAKUNIY("yakuniy", "Yakuniy test (Final)");

    private final String code;
    private final String displayName;

    TestType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }

    public static TestType fromCode(String code) {
        for (TestType tt : values()) {
            if (tt.code.equalsIgnoreCase(code)) return tt;
        }
        throw new IllegalArgumentException("Unknown test type code: " + code);
    }
}
