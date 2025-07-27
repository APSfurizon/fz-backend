BEGIN;
ALTER TABLE IF EXISTS membership_info DROP COLUMN IF EXISTS info_id_type;
ALTER TABLE IF EXISTS membership_info DROP COLUMN IF EXISTS info_id_number;
ALTER TABLE IF EXISTS membership_info DROP COLUMN IF EXISTS info_id_issuer;
ALTER TABLE IF EXISTS membership_info DROP COLUMN IF EXISTS info_id_expiry;
ALTER TABLE IF EXISTS membership_info DROP COLUMN IF EXISTS info_shirt_size;
DROP TYPE IF EXISTS shirt_size;
DROP TYPE IF EXISTS id_type;
COMMIT;