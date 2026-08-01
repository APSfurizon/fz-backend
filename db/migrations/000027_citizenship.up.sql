BEGIN;
ALTER TABLE IF EXISTS membership_info ADD COLUMN IF NOT EXISTS info_citizenship TEXT;
UPDATE membership_info SET info_citizenship=membership_info.info_birth_country WHERE info_citizenship IS NULL;
ALTER TABLE membership_info ALTER COLUMN info_citizenship SET NOT NULL;
COMMIT;