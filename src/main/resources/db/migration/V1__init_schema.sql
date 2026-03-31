-- Blog API - Schema Initialization Script
-- PostgreSQL 15+

-- Enums
CREATE TYPE post_status AS ENUM ('DRAFT', 'PUBLISHED', 'SCHEDULED', 'ARCHIVED');
CREATE TYPE user_role AS ENUM ('ADMIN', 'AUTHOR', 'EDITOR');

-- Authors
CREATE TABLE authors (
    id         BIGSERIAL PRIMARY KEY,
    user_name  VARCHAR(255) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(255) NOT NULL,
    slug       VARCHAR(255) NOT NULL UNIQUE,
    bio        TEXT,
    avatar_url VARCHAR(255),
    website    VARCHAR(255),
    github     VARCHAR(255),
    x          VARCHAR(255),
    linkedin   VARCHAR(255),
    role       VARCHAR(50)  NOT NULL DEFAULT 'AUTHOR',
    active     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

-- Posts
CREATE TABLE posts (
    id               BIGSERIAL PRIMARY KEY,
    slug             VARCHAR(255) NOT NULL UNIQUE,
    title            VARCHAR(500) NOT NULL,
    content          TEXT         NOT NULL,
    excerpt          TEXT,
    featured_image   VARCHAR(255),
    status           VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    published_at     TIMESTAMP,
    author_id        BIGINT       NOT NULL REFERENCES authors(id),
    views_count      INTEGER      NOT NULL DEFAULT 0,
    meta_description VARCHAR(255),
    meta_keywords    VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at       TIMESTAMP
);

-- Indexes declared on @Table
CREATE INDEX idx_post_slug         ON posts(slug);
CREATE INDEX idx_post_status       ON posts(status);
CREATE INDEX idx_post_published_at ON posts(published_at);

-- Categories
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    slug        VARCHAR(255) NOT NULL  UNIQUE,
    description TEXT,
    icon        VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL
);

-- Join table
CREATE TABLE post_categories (
    post_id     BIGINT NOT NULL REFERENCES posts(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    PRIMARY KEY (post_id, category_id)
);