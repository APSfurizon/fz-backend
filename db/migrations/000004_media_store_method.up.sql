BEGIN;
ALTER TABLE IF EXISTS media ADD COLUMN IF NOT EXISTS media_store_method INT4 NOT NULL default 0;
ALTER TABLE IF EXISTS media ADD CONSTRAINT media_store_method_check CHECK (((media_store_method >= 0) AND (media_store_method <= 0)));
COMMIT;