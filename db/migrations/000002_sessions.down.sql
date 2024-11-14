DROP INDEX IF EXISTS sessions_user_id_idx;
DROP INDEX IF EXISTS sessions_created_at_idx;
DROP INDEX IF EXISTS sessions_expires_at_idx;

DROP TABLE IF EXISTS sessions;
