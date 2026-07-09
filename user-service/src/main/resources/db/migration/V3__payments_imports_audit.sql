-- ============================================================
-- V3: Payment Requests, Import Jobs, and Audit Logs
-- ============================================================

-- ─── 1. Payment Requests ─────────────────────────────────────

CREATE TABLE IF NOT EXISTS payment_requests (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    university_id   UUID        REFERENCES universities(id) ON DELETE SET NULL,

    -- Snapshot of user's profile at time of payment
    major           TEXT,
    grade           SMALLINT,
    study_method    TEXT,
    test_type       TEXT,

    -- Payment details
    amount          INTEGER     NOT NULL DEFAULT 15000,  -- in UZS
    receipt_file_id TEXT        NOT NULL,                -- Telegram fileId
    receipt_storage_path TEXT,                           -- MinIO path (if stored)
    status          TEXT        NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED

    -- Timestamps
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at     TIMESTAMPTZ,
    reviewed_by     BIGINT      REFERENCES admins(telegram_id) ON DELETE SET NULL
);

-- ─── 2. Question Import Jobs ─────────────────────────────────

CREATE TABLE IF NOT EXISTS import_jobs (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id        BIGINT      NOT NULL REFERENCES users(id),
    university_id   UUID        REFERENCES universities(id) ON DELETE SET NULL,
    category_id     UUID        REFERENCES categories(id) ON DELETE SET NULL,

    -- Source file info
    file_path       TEXT        NOT NULL,               -- MinIO path
    original_filename TEXT,
    file_format     TEXT,                               -- docx, pdf, txt, csv, xlsx, md, html, rtf

    -- Job status
    status          TEXT        NOT NULL DEFAULT 'UPLOADED', -- UPLOADED, PARSING, PREVIEW_READY, CONFIRMED, IMPORTING, COMPLETED, FAILED, CANCELLED

    -- Parse results (populated after parsing)
    detected_count  INTEGER     DEFAULT 0,
    valid_count     INTEGER     DEFAULT 0,
    duplicate_count INTEGER     DEFAULT 0,
    error_count     INTEGER     DEFAULT 0,
    imported_count  INTEGER     DEFAULT 0,

    error_message   TEXT,

    -- Timestamps
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ
);

-- Parsed questions (temporary storage during import preview)
CREATE TABLE IF NOT EXISTS parsed_questions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id          UUID        NOT NULL REFERENCES import_jobs(id) ON DELETE CASCADE,

    question_text   TEXT        NOT NULL,
    option_a        TEXT        NOT NULL,
    option_b        TEXT        NOT NULL,
    option_c        TEXT        NOT NULL,
    option_d        TEXT        NOT NULL,
    explanation     TEXT,

    is_valid        BOOLEAN     NOT NULL DEFAULT TRUE,
    is_duplicate    BOOLEAN     NOT NULL DEFAULT FALSE,
    validation_error TEXT,

    source_line_number INTEGER
);

-- Import errors log
CREATE TABLE IF NOT EXISTS import_errors (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id      UUID    NOT NULL REFERENCES import_jobs(id) ON DELETE CASCADE,
    line_number INTEGER,
    error_msg   TEXT    NOT NULL,
    raw_content TEXT
);

-- ─── 3. Audit Logs ───────────────────────────────────────────

CREATE TABLE IF NOT EXISTS audit_logs (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id    BIGINT      NOT NULL,
    action      TEXT        NOT NULL,   -- APPROVE_PAYMENT, REJECT_PAYMENT, ADD_QUESTION, DELETE_CATEGORY, BROADCAST, etc.
    entity_type TEXT,                   -- PaymentRequest, Question, Category, User
    entity_id   TEXT,
    details     JSONB,
    ip_address  TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 4. Indexes ──────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS idx_payment_requests_user
    ON payment_requests(user_id);

CREATE INDEX IF NOT EXISTS idx_payment_requests_status
    ON payment_requests(status, university_id);

CREATE INDEX IF NOT EXISTS idx_payment_requests_pending
    ON payment_requests(university_id, created_at DESC)
    WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_import_jobs_admin
    ON import_jobs(admin_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_parsed_questions_job
    ON parsed_questions(job_id);

CREATE INDEX IF NOT EXISTS idx_audit_logs_admin
    ON audit_logs(admin_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id);
