-- Add the current correct option index for databases that already applied an
-- earlier V1 migration before this column existed.
ALTER TABLE quiz_sessions
    ADD COLUMN IF NOT EXISTS current_correct_option_idx SMALLINT;
