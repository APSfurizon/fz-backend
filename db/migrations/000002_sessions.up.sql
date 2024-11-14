CREATE TABLE IF NOT EXISTS sessions
(
    id                      uuid PRIMARY KEY NOT NULL,
    user_agent              varchar(255)     NULL,
    created_by_ip_address   varchar(16)      NOT NULL, -- if we don't need any IP tracking -> Delete it --
    last_used_by_ip_address varchar(16)      NOT NULL, -- if we don't need any IP tracking -> Delete it --
    user_id                 int8             NOT NULL,
    created_at              timestamptz      NOT NULL,
    modified_at             timestamptz      NOT NULL,
    expires_at              timestamptz      NOT NULL
);

CREATE INDEX IF NOT EXISTS sessions_user_id_idx ON sessions (user_id);
CREATE INDEX IF NOT EXISTS sessions_created_at_idx ON sessions USING BRIN (created_at);
CREATE INDEX IF NOT EXISTS sessions_expires_at_idx ON sessions USING BRIN (expires_at);
