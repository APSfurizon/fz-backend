BEGIN;

DROP INDEX IF EXISTS notification_identifier_index;
DROP TABLE IF EXISTS one_time_notifications;

COMMIT;