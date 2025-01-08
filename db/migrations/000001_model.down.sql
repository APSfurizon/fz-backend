DROP INDEX IF EXISTS authentications_email_idx;
DROP INDEX IF EXISTS authentication_hashed_password;

DROP TRIGGER IF EXISTS delete_room_guests_on_order_deletion ON orders;
DROP FUNCTION IF EXISTS deleteRoomGuests();

DROP TABLE IF EXISTS exchange_confirmation_status;
DROP TABLE IF EXISTS room_guests;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS membership_cards;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS fursuits_events;
DROP TABLE IF EXISTS fursuits;
DROP TABLE IF EXISTS membership_info;
DROP TABLE IF EXISTS email_confirmation_request;
DROP TABLE IF EXISTS reset_password_requests;
DROP TABLE IF EXISTS authentications;
DROP TABLE IF EXISTS user_has_role;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS media;
DROP TABLE IF EXISTS events;

DROP SEQUENCE IF EXISTS membership_cards_id_in_years;

