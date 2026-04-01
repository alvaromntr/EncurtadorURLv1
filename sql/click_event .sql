CREATE TABLE click_event (
    id BIGINT PRIMARY KEY,
    short_url_id BIGINT REFERENCES short_url(id),
    clicked_at TIMESTAMP DEFAULT NOW(),
    ip_address VARCHAR(45),
    user_agent TEXT,
    referer TEXT
);