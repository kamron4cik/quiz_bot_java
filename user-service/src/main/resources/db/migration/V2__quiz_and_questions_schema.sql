-- ============================================================
-- V2: Questions, Categories, and Quiz Session Schema
-- ============================================================

-- ─── 1. Categories ───────────────────────────────────────────

CREATE TABLE IF NOT EXISTS categories (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name            TEXT        NOT NULL,
    description     TEXT,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,

    -- University-scoping (multi-tenant)
    university_id   UUID        REFERENCES universities(id) ON DELETE CASCADE,
    major           TEXT,
    grade           SMALLINT,
    study_method    TEXT,
    test_type       TEXT,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (name, university_id, major, grade, study_method, test_type)
);

-- ─── 2. Questions ────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS questions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id     UUID        NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    university_id   UUID        REFERENCES universities(id) ON DELETE SET NULL,

    question_text   TEXT        NOT NULL,
    option_a        TEXT        NOT NULL,   -- ALWAYS the correct answer
    option_b        TEXT        NOT NULL,
    option_c        TEXT        NOT NULL,
    option_d        TEXT        NOT NULL,
    correct_answer  CHAR(1)     NOT NULL DEFAULT 'A' CHECK (correct_answer IN ('A','B','C','D')),
    explanation     TEXT,

    created_by      BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    version         INTEGER     NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Full-text search vector (auto-maintained by trigger below)
    search_vector   TSVECTOR    GENERATED ALWAYS AS
        (to_tsvector('simple', coalesce(question_text, ''))) STORED
);

-- ─── 3. Quiz Sessions ────────────────────────────────────────

CREATE TYPE quiz_status AS ENUM ('ACTIVE', 'PAUSED', 'COMPLETED', 'STOPPED', 'TIMEOUT');

CREATE TABLE IF NOT EXISTS quiz_sessions (
    id                          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id                 UUID        NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    university_id               UUID        REFERENCES universities(id) ON DELETE SET NULL,

    -- Configuration
    status                      quiz_status NOT NULL DEFAULT 'ACTIVE',
    mode                        TEXT        NOT NULL DEFAULT 'random',           -- random, sequential
    question_count              INTEGER     NOT NULL,
    time_per_question           INTEGER     NOT NULL,                            -- in seconds
    question_offset             INTEGER     NOT NULL DEFAULT 0,                  -- for sequential mode

    -- Progress (stored as UUID array — ordered question sequence)
    question_ids                UUID[]      NOT NULL,
    current_question_index      INTEGER     NOT NULL DEFAULT 0,
    current_question_sent_at    TIMESTAMPTZ,
    last_message_id             BIGINT,

    -- Current question Telegram state (for poll-based answers)
    current_poll_id             TEXT,
    current_shuffled_options    TEXT[],                                          -- shuffled order labels
    current_correct_option_index INTEGER,

    -- Results
    total_correct               INTEGER     NOT NULL DEFAULT 0,
    total_wrong                 INTEGER     NOT NULL DEFAULT 0,
    score                       NUMERIC(5,2) NOT NULL DEFAULT 0,

    -- Timestamps
    started_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at                 TIMESTAMPTZ
);

-- ─── 4. Quiz Answers ─────────────────────────────────────────

CREATE TABLE IF NOT EXISTS quiz_answers (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id          UUID        NOT NULL REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    question_id         UUID        NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    selected_answer     CHAR(1),    -- 'A','B','C','D' or NULL on timeout
    is_correct          BOOLEAN     NOT NULL DEFAULT FALSE,
    response_time_sec   INTEGER     NOT NULL DEFAULT 0,
    answered_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 5. Indexes ──────────────────────────────────────────────

-- Category filtering (most common query pattern)
CREATE INDEX IF NOT EXISTS idx_categories_scope
    ON categories(university_id, major, grade, study_method, test_type, is_active);

CREATE INDEX IF NOT EXISTS idx_categories_active
    ON categories(is_active) WHERE is_active = TRUE;

-- Question lookup
CREATE INDEX IF NOT EXISTS idx_questions_category
    ON questions(category_id);

CREATE INDEX IF NOT EXISTS idx_questions_university
    ON questions(university_id);

CREATE INDEX IF NOT EXISTS idx_questions_fts
    ON questions USING GIN(search_vector);

-- Quiz session lookup (most critical for bot performance)
CREATE UNIQUE INDEX IF NOT EXISTS idx_quiz_sessions_active_user
    ON quiz_sessions(user_id)
    WHERE status = 'ACTIVE';  -- Enforces single active session per user at DB level

CREATE INDEX IF NOT EXISTS idx_quiz_sessions_user_status
    ON quiz_sessions(user_id, status);

CREATE INDEX IF NOT EXISTS idx_quiz_sessions_poll_id
    ON quiz_sessions(current_poll_id)
    WHERE current_poll_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_quiz_sessions_active
    ON quiz_sessions(status, current_question_sent_at)
    WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_quiz_sessions_leaderboard
    ON quiz_sessions(university_id, user_id, total_correct, finished_at)
    WHERE status IN ('COMPLETED', 'STOPPED', 'TIMEOUT');

-- Quiz answers
CREATE INDEX IF NOT EXISTS idx_quiz_answers_session
    ON quiz_answers(session_id);

-- ─── 6. Updated-at trigger ───────────────────────────────────

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE OR REPLACE TRIGGER trg_questions_updated_at
    BEFORE UPDATE ON questions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
