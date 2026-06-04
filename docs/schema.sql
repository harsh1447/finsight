-- FinSight Database Schema
-- Run this file to recreate the entire database from scratch
-- Created by: Harsh Singh

-- Create Database (run this separately if needed)
-- CREATE DATABASE finsight;

-- ========================
-- TABLE: users
-- ========================
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    currency      VARCHAR(10) DEFAULT 'INR',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- TABLE: categories
-- ========================
CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    icon        VARCHAR(50),
    color_hex   VARCHAR(7),
    is_default  BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- TABLE: transactions
-- ========================
CREATE TABLE IF NOT EXISTS transactions (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id      BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    title            VARCHAR(200) NOT NULL,
    amount           DECIMAL(12, 2) NOT NULL,
    type             VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    transaction_date DATE NOT NULL,
    description      TEXT,
    source           VARCHAR(50) DEFAULT 'MANUAL',
    merchant_name    VARCHAR(100),
    is_recurring     BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- TABLE: uploaded_statements
-- ========================
CREATE TABLE IF NOT EXISTS uploaded_statements (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name      VARCHAR(255) NOT NULL,
    file_type      VARCHAR(10) CHECK (file_type IN ('CSV', 'PDF')),
    file_path      VARCHAR(500),
    status         VARCHAR(20) DEFAULT 'PROCESSING'
                   CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
    total_records  INTEGER DEFAULT 0,
    parsed_records INTEGER DEFAULT 0,
    uploaded_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- TABLE: budget_limits
-- ========================
CREATE TABLE IF NOT EXISTS budget_limits (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id   BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    monthly_limit DECIMAL(12, 2) NOT NULL,
    month         INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    year          INTEGER NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, category_id, month, year)
);

-- ========================
-- TABLE: ai_insights
-- ========================
CREATE TABLE IF NOT EXISTS ai_insights (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    insight_type VARCHAR(50),
    content      TEXT NOT NULL,
    month        INTEGER,
    year         INTEGER,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- INDEXES
-- ========================
CREATE INDEX IF NOT EXISTS idx_transactions_user_id
    ON transactions(user_id);

CREATE INDEX IF NOT EXISTS idx_transactions_date
    ON transactions(transaction_date);

CREATE INDEX IF NOT EXISTS idx_transactions_user_date
    ON transactions(user_id, transaction_date);

CREATE INDEX IF NOT EXISTS idx_transactions_merchant
    ON transactions(merchant_name);

CREATE INDEX IF NOT EXISTS idx_users_email
    ON users(email);

-- ========================
-- SEED DATA: categories
-- ========================
INSERT INTO categories (name, icon, color_hex) VALUES
('Food', '🍔', '#FF6B6B'),
('Travel', '✈️', '#4ECDC4'),
('Shopping', '🛍️', '#45B7D1'),
('Bills', '📄', '#96CEB4'),
('Entertainment', '🎬', '#FFEAA7'),
('Healthcare', '🏥', '#DDA0DD'),
('Education', '📚', '#98D8C8'),
('Other', '📦', '#B0B0B0')
ON CONFLICT (name) DO NOTHING;