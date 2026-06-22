CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE merchant_status AS ENUM (
    'PENDING',
    'ACTIVE',
    'SUSPENDED',
    'DEACTIVATED'
);

CREATE TABLE merchants (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_name VARCHAR(255)        NOT NULL,
    email         VARCHAR(255)        NOT NULL UNIQUE,
    password_hash VARCHAR(255)        NOT NULL,
    status        merchant_status     NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_merchants_email ON merchants(email);