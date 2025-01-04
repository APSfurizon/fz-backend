CREATE TABLE IF NOT EXISTS events
(
    id               int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    event_slug       varchar(255)                                                                                                             NOT NULL,
    event_date_to    timestamptz                                                                                                              NULL,
    event_date_from  timestamptz                                                                                                              NULL,
    event_is_current bool                                                                                                                     NOT NULL,
    event_public_url text                                                                                                                     NOT NULL,
    event_names_json json                                                                                                                     NULL,
    CONSTRAINT event_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS media
(
    media_id   int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    media_path text                                                                                                                     NOT NULL,
    media_type varchar(255)                                                                                                             NOT NULL,
    CONSTRAINT media_pkey PRIMARY KEY (media_id)
);

CREATE TABLE IF NOT EXISTS roles
(
    role_id       int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    display_name  text                                                                                                                     NOT NULL,
    internal_name text                                                                                                                     NULL,
    CONSTRAINT roles_pk PRIMARY KEY (role_id),
    CONSTRAINT roles_unique_internal_name UNIQUE (internal_name)
);

CREATE TABLE IF NOT EXISTS permission
(
    role_id          int8 NOT NULL,
    permission_value int8 NOT NULL,
    CONSTRAINT permission_pk PRIMARY KEY (role_id, permission_value),
    CONSTRAINT permission_role_fk FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS users
(
    user_id           int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    user_fursona_name varchar(64)                                                                                                              NOT NULL,
    user_locale       varchar(8) DEFAULT 'en-us'::character varying                                                                            NULL,
    media_id_propic   int8                                                                                                                     NULL,
    CONSTRAINT users_pkey PRIMARY KEY (user_id),
    CONSTRAINT user_media_fk FOREIGN KEY (media_id_propic) REFERENCES media (media_id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS user_has_role
(
    user_id       int8 NOT NULL,
    role_id       int8 NOT NULL,
    temp_event_id int8 NOT NULL, -- Used for temp roles (EG dealers den, panel host, meeting host, etc) --
    CONSTRAINT user_has_role_pk PRIMARY KEY (user_id, role_id),
    CONSTRAINT user_has_role_user_fk FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT user_has_role_role_fk FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT user_has_role_event_fk FOREIGN KEY (temp_event_id) REFERENCES events (id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS authentications
(
    authentication_id                              int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    authentication_email                           varchar(255)                                                                                                             NOT NULL,
    authentication_email_verification_creation_ms  timestamptz                                                                                                              NULL DEFAULT NOW(),
    authentication_disabled                        bool                                                                                                                     NOT NULL DEFAULT false,
    authentication_hashed_password                 text                                                                                                                     NOT NULL,
    authentication_token                           varchar(255)                                                                                                             NULL,
    user_id                                        int8                                                                                                                     NOT NULL,
    CONSTRAINT authentications_pkey PRIMARY KEY (authentication_id),
    CONSTRAINT authentications_unique_user_id UNIQUE (user_id),
    CONSTRAINT authentications_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS authentications_email_idx ON authentications USING HASH (authentication_email);
CREATE INDEX IF NOT EXISTS authentication_hashed_password ON authentications USING HASH (authentication_hashed_password);

CREATE TABLE IF NOT EXISTS membership_info
(
    id                     int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    user_id                int8                                                                                                                     NOT NULL,
    info_first_name        text                                                                                                                     NOT NULL,
    info_last_name         text                                                                                                                     NOT NULL,
    info_fiscal_code       varchar(16)                                                                                                              NULL,
    info_birth_city        text                                                                                                                     NOT NULL,
    info_birth_region      text                                                                                                                     NULL,
    info_birth_country     text                                                                                                                     NOT NULL,
    info_birthday          date                                                                                                                     NOT NULL,
    info_address           text                                                                                                                     NOT NULL,
    info_zip               varchar(16)                                                                                                              NOT NULL,
    info_city              text                                                                                                                     NOT NULL,
    info_region            text                                                                                                                     NULL,
    info_country           text                                                                                                                     NOT NULL,
    info_phone             text                                                                                                                     NOT NULL,
    last_updated_event_id  int8                                                                                                                     NULL,
    CONSTRAINT membership_info_id_pkey PRIMARY KEY (id),
    CONSTRAINT membership_info_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT membership_info_updated_event_id FOREIGN KEY (last_updated_event_id) REFERENCES events (id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS fursuits
(
    fursuit_id      int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    fursuit_name    varchar(255)                                                                                                             NOT NULL,
    fursuit_species varchar(255)                                                                                                             NULL,
    user_id         int8                                                                                                                     NOT NULL,
    media_id_propic int8                                                                                                                     NULL,
    CONSTRAINT fursuits_pkey PRIMARY KEY (fursuit_id),
    CONSTRAINT fursuits_media_fk FOREIGN KEY (media_id_propic) REFERENCES media (media_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fursuits_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS fursuits_events
(
    event_id   int8 NOT NULL,
    fursuit_id int8 NOT NULL,
    CONSTRAINT fursuits_events_pk PRIMARY KEY (event_id, fursuit_id),
    CONSTRAINT fursuits_events_event_fk FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fursuits_events_fursuit_fk FOREIGN KEY (fursuit_id) REFERENCES fursuits (fursuit_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS orders
(
    id                             int8 PRIMARY KEY             NOT NULL,
    order_code                     varchar(64)                  NOT NULL,
    order_answers_json             json                         NULL,
    order_status                   int2 DEFAULT 0               NOT NULL,
    order_answers_main_position_id int4                         NOT NULL,
    order_daily_days               int8                         NOT NULL,
    order_extra_days_type          int2                         NULL,
    order_room_capacity            int2                         NULL,
    order_hotel_location           varchar(255)                 NULL,
    has_membership                 bool                         NOT NULL,
    order_secret                   varchar(32)                  NULL, -- todo remove --
    order_sponsorship_type         int2                         NOT NULL,
    event_id                       int8                         NOT NULL,
    user_id                        int8 DEFAULT NULL            NULL,
    creation_ts                    timestamptz                  NOT NULL DEFAULT NOW(), -- just for stats reasons --
    CONSTRAINT orders_extra_days_check CHECK (((order_extra_days_type >= 0) AND (order_extra_days_type <= 3))),
    CONSTRAINT orders_sponsorship_check CHECK (((order_sponsorship_type >= 0) AND (order_sponsorship_type <= 2))),
    CONSTRAINT orders_status_check CHECK (((order_status >= 0) AND (order_status <= 3))),
    CONSTRAINT orders_events_id FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT orders_users_id FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS membership_cards
(
    card_db_id         int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    id_in_year         int4                                                                                                                     NOT NULL,
    issue_year         int2                                                                                                                     NOT NULL,
    user_id            int8                                                                                                                     NOT NULL,
    already_registered bool DEFAULT false                                                                                                       NOT NULL,
    created_for_order  int8                                                                                                                     NULL, -- this has to be nullable, because manual insertions not linked to orders are possible --
    CONSTRAINT cards_pkey PRIMARY KEY (card_db_id),
    CONSTRAINT card_user_fk FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT membership_cards_order_fk FOREIGN KEY (created_for_order) REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS rooms
(
    room_id        int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    room_confirmed bool                                                                                                                     NOT NULL DEFAULT FALSE,
    room_name      varchar(255)                                                                                                             NOT NULL,
    order_id       int                                                                                                                      NOT NULL,
    CONSTRAINT rooms_pkey PRIMARY KEY (room_id),
    CONSTRAINT rooms_orders_id FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS room_guests
(
    room_guest_id int8 GENERATED BY DEFAULT AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    user_id       int8                                                                                                                     NOT NULL,
    room_id       int8                                                                                                                     NOT NULL,
    CONSTRAINT room_guests_pkey PRIMARY KEY (room_guest_id),
    CONSTRAINT room_guests_rooms_fk FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT room_guests_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);