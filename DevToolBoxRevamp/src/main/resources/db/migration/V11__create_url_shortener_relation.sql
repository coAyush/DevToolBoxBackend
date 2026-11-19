CREATE SEQUENCE url_id_seq START WITH 10000000000 INCREMENT BY 1;

CREATE TABLE url (
    id               BIGINT        PRIMARY KEY,           -- nextval('url_id_seq') se aayega
    short_code       VARCHAR(20)   NOT NULL,
    original_url     VARCHAR(2048) NOT NULL,
    user_id          UUID        NOT NULL,
    is_custom_alias  BOOLEAN       NOT NULL DEFAULT FALSE,
    click_count      BIGINT        NOT NULL DEFAULT 0,
    last_clicked_at  TIMESTAMPTZ,
    expires_at       TIMESTAMPTZ,
    deleted_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version          BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT uk_url_short_code UNIQUE (short_code),
    CONSTRAINT fk_url_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_url_user_created ON url (user_id, created_at DESC);

CREATE INDEX idx_url_expires_at ON url (expires_at) WHERE expires_at IS NOT NULL;