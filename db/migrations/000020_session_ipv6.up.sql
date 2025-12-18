BEGIN;
ALTER TABLE sessions ALTER COLUMN last_used_by_ip_address TYPE varchar(40);
ALTER TABLE sessions ALTER COLUMN created_by_ip_address TYPE varchar(40);
COMMIT;