ALTER TABLE IF EXISTS roles DROP COLUMN IF EXISTS show_in_nosecount;
BEGIN;
UPDATE roles SET display_name = CAST(random() AS TEXT) WHERE display_name IS NULL;
ALTER TABLE roles ALTER COLUMN display_name SET NOT NULL;
ALTER TABLE roles ALTER COLUMN internal_name DROP NOT NULL;
COMMIT;