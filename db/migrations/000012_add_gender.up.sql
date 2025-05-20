BEGIN;
ALTER TABLE IF EXISTS membership_info ADD COLUMN IF NOT EXISTS info_document_sex "char" NOT NULL DEFAULT 'M';
ALTER TABLE IF EXISTS membership_info ADD CONSTRAINT info_document_sex_mf CHECK (
    (info_document_sex = 'M') OR (info_document_sex = 'm') OR
    (info_document_sex = 'F') OR (info_document_sex = 'f')
);
ALTER TABLE IF EXISTS membership_info ADD COLUMN IF NOT EXISTS info_gender varchar(64) NULL DEFAULT NULL;
COMMIT;