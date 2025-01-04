DROP INDEX IF EXISTS authentications_email_idx;
DROP INDEX IF EXISTS authentication_hashed_password;

DROP TABLE IF EXISTS room_guests;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS membership_cards;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS fursuits_events;
DROP TABLE IF EXISTS fursuits;
DROP TABLE IF EXISTS membership_info;
DROP TABLE IF EXISTS authentications;
DROP TABLE IF EXISTS user_has_role;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS media;
DROP TABLE IF EXISTS events;

DROP SEQUENCE IF EXISTS membership_cards_id_in_years;
