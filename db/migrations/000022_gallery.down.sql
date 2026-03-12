BEGIN;

DROP TABLE IF EXISTS upload_video_data;
DROP TABLE IF EXISTS upload_exif;
DROP TABLE IF EXISTS uploads;
DROP TYPE IF EXISTS upload_repost_permissions;
DROP TYPE IF EXISTS upload_status;
DROP TYPE IF EXISTS upload_type;

COMMIT;

