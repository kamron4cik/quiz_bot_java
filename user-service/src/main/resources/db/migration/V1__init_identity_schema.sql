-- ============================================================
-- V1: Core Identity & University Schema
-- Quiz Platform — Enterprise Java Redesign
-- Converted from: schema_v2.sql + v3_migration.sql
-- ============================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For full-text trigram search on questions

-- ─── 1. Universities ─────────────────────────────────────────

CREATE TABLE IF NOT EXISTS universities (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT        NOT NULL UNIQUE,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed initial universities (from V1 v3_migration.sql)
INSERT INTO universities (name) VALUES
    ('TMI (Toshkent Moliya Instituti)'),
    ('TDIU'),
    ('TEAM'),
    ('WIUT')
ON CONFLICT (name) DO NOTHING;

-- ─── 2. Users ────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id               BIGINT      PRIMARY KEY,               -- Telegram User ID
    username         TEXT,
    first_name       TEXT,
    last_name        TEXT,

    -- Academic profile (completed via 5-step wizard)
    university_id    UUID        REFERENCES universities(id) ON DELETE SET NULL,
    major            TEXT,
    grade            SMALLINT    CHECK (grade BETWEEN 1 AND 5),
    study_method     TEXT,       -- kunduzgi, kechki, sirtqi, masofaviy
    test_type        TEXT,       -- oraliq, yakuniy

    -- Access control
    has_paid         BOOLEAN     NOT NULL DEFAULT FALSE,

    -- Aggregate stats (denormalized for quick access)
    total_tests      INTEGER     NOT NULL DEFAULT 0,
    total_questions  INTEGER     NOT NULL DEFAULT 0,
    average_score    NUMERIC(5,2) NOT NULL DEFAULT 0,

    -- Timestamps
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_activity    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 3. Admins ───────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS admins (
    telegram_id     BIGINT      PRIMARY KEY,
    university_id   UUID        REFERENCES universities(id) ON DELETE CASCADE,  -- NULL for SUPER_ADMIN
    role            TEXT        NOT NULL DEFAULT 'ADMIN',   -- SUPER_ADMIN, ADMIN, MODERATOR
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 4. Indexes ──────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS idx_users_university_id
    ON users(university_id);

CREATE INDEX IF NOT EXISTS idx_users_last_activity
    ON users(last_activity DESC);

CREATE INDEX IF NOT EXISTS idx_users_has_paid
    ON users(has_paid) WHERE has_paid = TRUE;

CREATE INDEX IF NOT EXISTS idx_users_university_paid
    ON users(university_id, has_paid);
