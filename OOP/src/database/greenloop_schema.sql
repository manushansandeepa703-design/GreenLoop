-- ============================================================
--  GreenLoop Database Schema
--  Run: mysql -u root -p < greenloop_schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS greenloop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE greenloop;

-- ── Users ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    password     VARCHAR(64)  NOT NULL COMMENT 'SHA-256 hash',
    role         ENUM('Admin','Employee') NOT NULL DEFAULT 'Employee',
    is_active    TINYINT(1)   NOT NULL DEFAULT 1,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Default admin account (password: admin123)
INSERT IGNORE INTO users (username, full_name, email, password, role)
VALUES ('admin', 'System Admin', 'admin@greenloop.com',
        SHA2('admin123', 256), 'Admin');

-- Default employee account (password: emp123)
INSERT IGNORE INTO users (username, full_name, email, password, role)
VALUES ('employee', 'Test Employee', 'employee@greenloop.com',
        SHA2('emp123', 256), 'Employee');

-- ── Clients ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS clients (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    business_name  VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    email          VARCHAR(100),
    phone          VARCHAR(20),
    address        TEXT,
    status         ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── Products ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    product_id  VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(150) NOT NULL,
    category    VARCHAR(80),
    description TEXT,
    price       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    eco_rating  TINYINT      NOT NULL DEFAULT 3 COMMENT '1-5 rating',
    quantity    INT          NOT NULL DEFAULT 0,
    status      ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── Stock ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stock (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    product_id       INT          NOT NULL,
    quantity_on_hand INT          NOT NULL DEFAULT 0,
    reorder_level    INT          NOT NULL DEFAULT 10,
    supplier_name    VARCHAR(150),
    last_restock     DATE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ── Orders ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    client_id    INT           NOT NULL,
    order_date   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status       ENUM('Pending','Processing','Delivered','Cancelled') NOT NULL DEFAULT 'Pending',
    notes        TEXT,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- ── Order Items ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    order_id    INT           NOT NULL,
    product_id  INT           NOT NULL,
    quantity    INT           NOT NULL,
    unit_price  DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ── Delivery Agents ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS delivery_agents (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    full_name        VARCHAR(100) NOT NULL,
    nic              VARCHAR(20),
    phone            VARCHAR(20),
    email            VARCHAR(100),
    license_number   VARCHAR(30),
    vehicle_type     VARCHAR(50),
    vehicle_make     VARCHAR(50),
    vehicle_model    VARCHAR(50),
    vehicle_color    VARCHAR(30),
    vehicle_year     YEAR,
    status           ENUM('Available','Unavailable') NOT NULL DEFAULT 'Available',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── Deliveries ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS deliveries (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    order_id   INT NOT NULL,
    agent_id   INT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status     ENUM('Assigned','In Transit','Delivered','Failed') NOT NULL DEFAULT 'Assigned',
    notes      TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (agent_id) REFERENCES delivery_agents(id)
);

-- ── Email Logs ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS email_logs (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    recipient   VARCHAR(100) NOT NULL,
    subject     VARCHAR(255),
    email_type  VARCHAR(80),
    sent_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success     TINYINT(1) NOT NULL DEFAULT 1,
    error_msg   TEXT
);

-- ── App Settings ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS app_settings (
    setting_key   VARCHAR(50)  PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL
);

INSERT IGNORE INTO app_settings VALUES ('tax_rate', '0.00');
INSERT IGNORE INTO app_settings VALUES ('currency',  'Rs.');
INSERT IGNORE INTO app_settings VALUES ('company_name', 'GreenLoop');
