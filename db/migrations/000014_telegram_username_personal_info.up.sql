BEGIN;
ALTER TABLE IF EXISTS membership_info ADD COLUMN IF NOT EXISTS info_telegram_username TEXT;
UPDATE membership_info SET info_telegram_username='' WHERE info_telegram_username IS NULL;
ALTER TABLE membership_info ALTER COLUMN info_telegram_username SET NOT NULL;
COMMIT;