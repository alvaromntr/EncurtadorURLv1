CREATE TABLE short_url (
    id BIGSERIAL PRIMARY KEY,
    hash BIGINT UNIQUE NOT NULL,
    original_url TEXT NOT NULL,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    click_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);