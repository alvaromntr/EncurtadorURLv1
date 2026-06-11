-- =============================================
-- USERS
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status        VARCHAR(50)  NOT NULL DEFAULT 'INACTIVE',
    created_at    TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ
);

-- =============================================
-- ROLES
-- =============================================
CREATE TABLE IF NOT EXISTS roles (
    id   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (name) VALUES ('ADMIN')    ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('OPERADOR') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('USER')     ON CONFLICT (name) DO NOTHING;

-- =============================================
-- USER_ROLES
-- =============================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- =============================================
-- TWO_FACTOR_CODES
-- =============================================
CREATE TABLE IF NOT EXISTS two_factor_codes (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code       VARCHAR(10) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used       BOOLEAN     NOT NULL DEFAULT FALSE
);

-- =============================================
-- SHORT_URL
-- =============================================
CREATE TABLE IF NOT EXISTS short_url (
    id           BIGSERIAL    PRIMARY KEY,
    hash         BIGINT,
    original_url TEXT         NOT NULL,
    short_code   VARCHAR(20)  NOT NULL UNIQUE,
    click_count  BIGINT       NOT NULL DEFAULT 0,
    created_at   TIMESTAMP,
    user_id      UUID REFERENCES users(id) ON DELETE SET NULL
);

-- =============================================
-- CLICK_EVENT
-- =============================================
CREATE TABLE IF NOT EXISTS click_event (
    id         BIGSERIAL   PRIMARY KEY,
    url_id     BIGINT      NOT NULL REFERENCES short_url(id) ON DELETE CASCADE,
    clicked_at TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT,
    referer    TEXT
);