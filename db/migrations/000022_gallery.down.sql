BEGIN;

DROP TABLE IF EXISTS upload_video_data;
DROP TABLE IF EXISTS upload_exif;
DROP TABLE IF EXISTS uploads;
DROP TABLE IF EXISTS upload_progress_info;
DROP TYPE IF EXISTS upload_progress_info_type;
DROP TYPE IF EXISTS upload_repost_permissions;
DROP TYPE IF EXISTS upload_status;
DROP TYPE IF EXISTS upload_type;

ALTER TABLE IF EXISTS media DROP CONSTRAINT IF EXISTS media_store_method_check;
DELETE FROM media WHERE media_store_method > 0;
ALTER TABLE IF EXISTS media ADD CONSTRAINT media_store_method_check CHECK (((media_store_method >= 0) AND (media_store_method <= 0)));

COMMIT;

