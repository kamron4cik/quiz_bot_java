-- ============================================================
-- V1: Questions & Categories Schema
-- quiz-platform — question-service
-- ============================================================

-- ─── 1. Categories ───────────────────────────────────────────
-- Each category belongs to a university (university_id from user-service)
-- and groups related quiz questions together.

CREATE TABLE IF NOT EXISTS categories (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    university_id  UUID        NOT NULL,   -- FK references user-service.universities (cross-service)
    name           TEXT        NOT NULL,
    description    TEXT,
    is_active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (university_id, name)
);

CREATE INDEX IF NOT EXISTS idx_categories_university_id ON categories(university_id);
CREATE INDEX IF NOT EXISTS idx_categories_active ON categories(is_active) WHERE is_active = TRUE;

-- ─── 2. Questions ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS questions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id     UUID        NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    text            TEXT        NOT NULL,
    option_a        TEXT        NOT NULL,
    option_b        TEXT        NOT NULL,
    option_c        TEXT        NOT NULL,
    option_d        TEXT        NOT NULL,
    correct_answer  SMALLINT    NOT NULL CHECK (correct_answer BETWEEN 0 AND 3),
                                -- 0=A, 1=B, 2=C, 3=D
    explanation     TEXT,       -- optional explanation shown after answer
    image_url       TEXT,       -- optional image associated with the question
    difficulty      TEXT        NOT NULL DEFAULT 'MEDIUM',
                                -- EASY, MEDIUM, HARD
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_questions_category_id ON questions(category_id);
CREATE INDEX IF NOT EXISTS idx_questions_active ON questions(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_questions_category_active ON questions(category_id, is_active);

-- Full-text search support (uses pg_trgm extension from user-service migration)
CREATE INDEX IF NOT EXISTS idx_questions_text_trgm ON questions USING gin(text gin_trgm_ops);
