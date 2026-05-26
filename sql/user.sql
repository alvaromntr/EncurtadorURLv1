CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- TABELA USERS
-- =========================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,

    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    status user_status NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMP WITH TIME ZONE
);

// ALTERAÇÃO PARA SUPORTE A ENUMERADO DE STATUS DO USUÁRIO

ALTER TABLE users
ALTER COLUMN status
TYPE VARCHAR(20)
USING status::text;
