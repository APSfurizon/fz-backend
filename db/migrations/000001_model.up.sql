CREATE TABLE IF NOT EXISTS events
(
    id               int PRIMARY KEY NOT NULL,
    event_slug       varchar(255)    NOT NULL,
    event_date_to    timestamptz     NULL,
    event_date_from  timestamptz     NULL,
    event_is_current bool            NOT NULL,
    event_public_url text            NOT NULL,
    event_names_json json            NULL
);

CREATE TABLE IF NOT EXISTS "groups"
(
    group_id   int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    group_name varchar(255)                                                                                                             NOT NULL,
    CONSTRAINT groups_pkey PRIMARY KEY (group_id)
);

CREATE TABLE IF NOT EXISTS media
(
    media_id   int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    media_path text                                                                                                                     NULL,
    media_type varchar(255)                                                                                                             NULL,
    CONSTRAINT media_pkey PRIMARY KEY (media_id)
);

CREATE TABLE IF NOT EXISTS group_permissions
(
    group_id        int8 NOT NULL,
    permission_code text NOT NULL,
    CONSTRAINT group_permissions_pk PRIMARY KEY (permission_code, group_id)
);

CREATE TABLE IF NOT EXISTS users
(
    user_id           int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    user_fursona_name varchar(64)                                                                                                              NULL,
    user_locale       varchar(8) DEFAULT 'en-us'::character varying                                                                            NULL,
    user_secret       varchar(70)                                                                                                              NOT NULL,
    media_id_propic   int8                                                                                                                     NULL,
    CONSTRAINT users_pkey PRIMARY KEY (user_id),
    CONSTRAINT users_unique_secret UNIQUE (user_secret),
    CONSTRAINT user_media_fk FOREIGN KEY (media_id_propic) REFERENCES media (media_id)
);

CREATE TABLE IF NOT EXISTS authentications
(
    authentication_id             int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    authentication_email          varchar(255)                                                                                                             NOT NULL,
    authentication_email_verified bool                                                                                                                     NOT NULL DEFAULT false,
    authentication_2fa_enabled    bool                                                                                                                     NOT NULL DEFAULT false,
    authentication_disabled       bool                                                                                                                     NOT NULL DEFAULT false,
    authentication_expired        bool                                                                                                                     NULL, -- what does that mean? --
    authentication_from_oauth     bool                                                                                                                     NOT NULL DEFAULT false,
    authentication_password       text                                                                                                                     NOT NULL,
    authentication_token          varchar(255)                                                                                                             NULL,
    user_id                       int8                                                                                                                     NOT NULL ,
    CONSTRAINT authentications_pkey PRIMARY KEY (authentication_id),
    CONSTRAINT authentications_unique_user_id UNIQUE (user_id),
    CONSTRAINT authentications_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX authentications_email_idx ON authentications USING HASH (authentication_email);

CREATE TABLE IF NOT EXISTS membership_info
(
    user_id            int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    info_first_name    text                                                                                                                     NULL,
    info_last_name     text                                                                                                                     NULL,
    info_address       text                                                                                                                     NULL,
    info_zip           varchar(16)                                                                                                              NULL,
    info_city          text                                                                                                                     NULL,
    info_country       text                                                                                                                     NULL,
    info_tax_id        varchar(16)                                                                                                              NULL,
    info_birth_city    text                                                                                                                     NULL,
    info_birth_region  text                                                                                                                     NULL,
    info_birth_country text                                                                                                                     NULL,
    info_region        text                                                                                                                     NULL,
    info_phone         text                                                                                                                     NULL,
    CONSTRAINT membership_info_id_pkey PRIMARY KEY (user_id),
    CONSTRAINT membership_info_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS fursuits
(
    fursuit_id      int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    fursuit_name    varchar(255)                                                                                                             NULL,
    fursuit_species varchar(255)                                                                                                             NULL,
    user_id         int8                                                                                                                     NULL,
    media_id_propic int8                                                                                                                     NULL,
    CONSTRAINT fursuits_pkey PRIMARY KEY (fursuit_id),
    CONSTRAINT fursuits_media_fk FOREIGN KEY (media_id_propic) REFERENCES media (media_id),
    CONSTRAINT fursuits_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS fursuits_events
(
    event_id   int8 NOT NULL,
    fursuit_id int8 NOT NULL,
    CONSTRAINT fursuits_events_pk PRIMARY KEY (event_id, fursuit_id),
    CONSTRAINT fursuits_events_event_fk FOREIGN KEY (event_id) REFERENCES media (media_id),
    CONSTRAINT fursuits_events_fursuit_fk FOREIGN KEY (fursuit_id) REFERENCES fursuits (fursuit_id)
);

CREATE TABLE IF NOT EXISTS membership_cards
(
    card_db_id int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    card_year  int2                                                                                                                     NOT NULL,
    user_id    int8                                                                                                                     NOT NULL,
    CONSTRAINT cards_pkey PRIMARY KEY (card_db_id),
    CONSTRAINT card_user_fk FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS orders
(
    id                             int PRIMARY KEY NOT NULL,
    order_code                     varchar(64)     NOT NULL,
    order_answers_json             json            NULL,
    order_status                   int2 DEFAULT 0  NOT NULL,
    order_answers_main_position_id int4            NOT NULL,
    order_daily_days               int8            NOT NULL,
    order_extra_days_type          int2            NULL,
    order_room_capacity            int2            NULL,
    order_hotel_location           varchar(255),
    has_membership                 bool            NOT NULL,
    order_secret                   varchar(32)     NULL,
    order_sponsorship_type         int2            NULL,
    event_id                       int             NOT NULL,
    user_id                        int8            NULL,
    CONSTRAINT orders_extra_days_check CHECK (((order_extra_days_type >= 0) AND (order_extra_days_type <= 3))),
    CONSTRAINT orders_sponsorship_check CHECK (((order_sponsorship_type >= 0) AND (order_sponsorship_type <= 2))),
    CONSTRAINT orders_status_check CHECK (((order_status >= 0) AND (order_status <= 3))),
    CONSTRAINT orders_events_id FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT orders_users_id FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS rooms
(
    room_id        int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    room_confirmed bool                                                                                                                     NULL,
    room_name      varchar(255)                                                                                                             NULL,
    order_id       int                                                                                                                      NULL,
    CONSTRAINT rooms_pkey PRIMARY KEY (room_id),
    CONSTRAINT rooms_orders_id FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE IF NOT EXISTS user_group
(
    group_id int8 NULL,
    user_id  int8 NULL,
    CONSTRAINT user_group_groups_fk FOREIGN KEY (group_id) REFERENCES "groups" (group_id),
    CONSTRAINT user_group_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS room_guests
(
    room_guest_id int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    user_id       int8                                                                                                                     NULL,
    room_id       int8                                                                                                                     NULL,
    CONSTRAINT room_guests_pkey PRIMARY KEY (room_guest_id),
    CONSTRAINT room_guests_rooms_fk FOREIGN KEY (room_id) REFERENCES rooms (room_id),
    CONSTRAINT room_guests_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id)
);