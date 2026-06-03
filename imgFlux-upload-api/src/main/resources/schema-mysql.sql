-- ImgFlux Upload API Database Schema
-- MySQL Database

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    team_name VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Images table
CREATE TABLE IF NOT EXISTS images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_hash VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(255),
    width INT,
    height INT,
    created_at DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_file_hash (file_hash),
    INDEX idx_user_file_hash (user_id, file_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Insert sample users (passwords should be hashed in production)
INSERT INTO users (username, password, company_name, team_name, created_at, updated_at) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MediaForge', 'Engineering', NOW(), NOW()),
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MediaForge', 'Marketing', NOW(), NOW());
