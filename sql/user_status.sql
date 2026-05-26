-- Extensão para geração automática de UUIDs no PostgreSQL
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Ajuste os valores conforme os enums Java reais
CREATE TYPE user_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'BLOCKED'
);