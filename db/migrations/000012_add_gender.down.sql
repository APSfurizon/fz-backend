BEGIN;
ALTER TABLE IF EXISTS membership_info DROP COLUMN IF EXISTS info_gender;
ALTER TABLE membership_info DROP CONSTRAINT IF EXISTS info_document_sex_mf;
ALTER TABLE IF EXISTS membership_info DROP COLUMN IF EXISTS info_document_sex;
COMMIT;