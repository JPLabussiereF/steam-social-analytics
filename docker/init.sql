-- Script de inicialização do banco de dados Steam Analytics
-- Criação de extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Usuários
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    steam_id BIGINT UNIQUE NOT NULL,
    username VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    profile_url TEXT,
    avatar_url TEXT,
    country_code CHAR(2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_login TIMESTAMP WITH TIME ZONE,
    profile_visibility INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    version BIGINT DEFAULT 0
);

-- Jogos
CREATE TABLE IF NOT EXISTS games (
    game_id BIGSERIAL PRIMARY KEY,
    steam_app_id INTEGER UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE,
    developer VARCHAR(255),
    publisher VARCHAR(255),
    price_initial DECIMAL(10,2),
    price_current DECIMAL(10,2),
    tags JSONB,
    categories JSONB,
    genres JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Biblioteca de jogos do usuário
CREATE TABLE IF NOT EXISTS user_game_library (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    game_id BIGINT REFERENCES games(game_id) ON DELETE CASCADE,
    playtime_total INTEGER DEFAULT 0, -- em minutos
    playtime_2weeks INTEGER DEFAULT 0, -- em minutos
    purchased_at TIMESTAMP WITH TIME ZONE,
    last_played TIMESTAMP WITH TIME ZONE,
    UNIQUE(user_id, game_id)
);

-- Sistema de amizades
CREATE TABLE IF NOT EXISTS friendships (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    addressee_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'pending', -- pending, accepted, blocked
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(requester_id, addressee_id),
    CONSTRAINT different_users CHECK (requester_id != addressee_id)
);

-- Índices para otimização
CREATE INDEX IF NOT EXISTS idx_user_game_library_user_playtime ON user_game_library(user_id, playtime_total DESC);
CREATE INDEX IF NOT EXISTS idx_user_game_library_game_playtime ON user_game_library(game_id, playtime_total DESC);
CREATE INDEX IF NOT EXISTS idx_friendships_requester_status ON friendships(requester_id, status);
CREATE INDEX IF NOT EXISTS idx_friendships_addressee_status ON friendships(addressee_id, status);
CREATE INDEX IF NOT EXISTS idx_games_tags ON games USING GIN(tags);
CREATE INDEX IF NOT EXISTS idx_games_categories ON games USING GIN(categories);
CREATE INDEX IF NOT EXISTS idx_users_steam_id ON users(steam_id);
CREATE INDEX IF NOT EXISTS idx_games_steam_app_id ON games(steam_app_id);

-- Dados de exemplo para desenvolvimento
INSERT INTO users (steam_id, username, display_name) VALUES
(76561198000000001, 'testuser1', 'Test User 1'),
(76561198000000002, 'testuser2', 'Test User 2')
ON CONFLICT (steam_id) DO NOTHING;

-- Jogos de exemplo
INSERT INTO games (steam_app_id, name, description, developer, publisher) VALUES
(730, 'Counter-Strike: Global Offensive', 'FPS competitivo', 'Valve', 'Valve'),
(570, 'Dota 2', 'MOBA', 'Valve', 'Valve'),
(440, 'Team Fortress 2', 'FPS', 'Valve', 'Valve')
ON CONFLICT (steam_app_id) DO NOTHING;