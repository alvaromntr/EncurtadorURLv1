CREATE TABLE click_event (

    id BIGSERIAL PRIMARY KEY,

    url_id BIGINT NOT NULL,

    clicked_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    ip_address TEXT,

    user_agent TEXT,

    referer TEXT,

    CONSTRAINT fk_click_event_url
        FOREIGN KEY (url_id)
        REFERENCES short_url(id)
        ON DELETE CASCADE
);