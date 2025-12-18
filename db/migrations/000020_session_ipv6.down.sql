BEGIN;

UPDATE sessions SET last_used_by_ip_address = left(last_used_by_ip_address, 16) WHERE char_length(last_used_by_ip_address) > 16;
ALTER TABLE sessions ALTER COLUMN last_used_by_ip_address TYPE varchar(16);

UPDATE sessions SET created_by_ip_address = left(created_by_ip_address, 16) WHERE char_length(created_by_ip_address) > 16;
ALTER TABLE sessions ALTER COLUMN created_by_ip_address TYPE varchar(16);

COMMIT;