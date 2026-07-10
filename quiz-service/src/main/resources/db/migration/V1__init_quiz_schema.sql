-- ============================================================
-- V1: Quiz Sessions Schema
-- quiz-platform — quiz-service
-- ============================================================

CREATE TABLE IF NOT EXISTS quiz_sessions (
    id                          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     BIGINT      NOT NULL,
    category_id                 UUID        NOT NULL,
    university_id               UUID,

    -- Configuration
    mode                        TEXT        NOT NULL DEFAULT 'RANDOM',
    question_count              INTEGER     NOT NULL,
    time_per_question_seconds   INTEGER     NOT NULL DEFAULT 30,
    question_offset             INTEGER     NOT NULL DEFAULT 0,

    -- Questions (ordered JSON array of UUIDs)
    question_ids                JSONB       NOT NULL DEFAULT '[]',
    current_question_index      INTEGER     NOT NULL DEFAULT 0,

    -- Current question state (cleared after each answer/timeout)
    current_poll_id             TEXT,
    current_correct_option_idx  SMALLINT,
    current_question_sent_at    TIMESTAMPTZ,
    last_message_id             BIGINT,

    -- Status
    status                      TEXT        NOT NULL DEFAULT 'ACTIVE',
                                            -- ACTIVE, PAUSED, COMPLETED, STOPPED, TIMEOUT

    -- Results
    total_correct               INTEGER     NOT NULL DEFAULT 0,
    total_wrong                 INTEGER     NOT NULL DEFAULT 0,
    score                       NUMERIC(5,2) NOT NULL DEFAULT 0,

    -- Timestamps
    started_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at                 TIMESTAMPTZ
);

-- One active session per user maximum (enforce at DB level)
CREATE UNIQUE INDEX IF NOT EXISTS uq_quiz_sessions_active_user
    ON quiz_sessions(user_id) WHERE status = 'ACTIVE';

-- One paused session per user maximum
CREATE UNIQUE INDEX IF NOT EXISTS uq_quiz_sessions_paused_user
    ON quiz_sessions(user_id) WHERE status = 'PAUSED';

CREATE INDEX IF NOT EXISTS idx_quiz_sessions_user_id
    ON quiz_sessions(user_id);

CREATE INDEX IF NOT EXISTS idx_quiz_sessions_status
    ON quiz_sessions(status) WHERE status IN ('ACTIVE', 'PAUSED');

-- User stats denormalized view (used for leaderboard queries)
CREATE TABLE IF NOT EXISTS user_quiz_stats (
    user_id                BIGINT      PRIMARY KEY,
    university_id          UUID,
    tests_completed        INTEGER     NOT NULL DEFAULT 0,
    questions_solved       INTEGER     NOT NULL DEFAULT 0,
    total_correct          INTEGER     NOT NULL DEFAULT 0,
    average_score          NUMERIC(5,2) NOT NULL DEFAULT 0,
    best_score             NUMERIC(5,2) NOT NULL DEFAULT 0,
    total_study_time_sec   BIGINT      NOT NULL DEFAULT 0,
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_quiz_stats_university
    ON user_quiz_stats(university_id, average_score DESC);

CREATE INDEX IF NOT EXISTS idx_user_quiz_stats_global
    ON user_quiz_stats(average_score DESC);
