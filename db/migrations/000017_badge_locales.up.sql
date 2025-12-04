BEGIN;
ALTER TABLE users ADD user_language varchar(8) NULL;
UPDATE users SET user_language='en_GB' WHERE user_language IS NULL;
ALTER TABLE users ALTER COLUMN user_language SET NOT NULL;
COMMIT;