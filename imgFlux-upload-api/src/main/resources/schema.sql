-- ImgFlux Upload API Database Schema
-- H2 Database

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    team_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Images table
CREATE TABLE IF NOT EXISTS images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_hash VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(255),
    width INTEGER,
    height INTEGER,
    created_at TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_images_user_id ON images(user_id);
CREATE INDEX IF NOT EXISTS idx_images_file_hash ON images(file_hash);
CREATE INDEX IF NOT EXISTS idx_images_user_file_hash ON images(user_id, file_hash);

-- Insert sample users (passwords should be hashed in production)
INSERT INTO users (username, password, company_name, team_name, created_at, updated_at) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MediaForge', 'Engineering', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MediaForge', 'Marketing', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
