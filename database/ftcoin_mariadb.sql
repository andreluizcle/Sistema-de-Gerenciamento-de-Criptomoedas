-- Script de criacao do banco FT-Coin no MariaDB
-- Executar com privilegios de DBA.
-- Conforme o enunciado: tipo `decimal` para valores monetarios e `varchar` para textos.

CREATE DATABASE IF NOT EXISTS ftcoin
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ftcoin;

CREATE TABLE IF NOT EXISTS carteira (
    id           INT          NOT NULL,
    nome_titular VARCHAR(120) NOT NULL,
    corretora    VARCHAR(120) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS movimentacao (
    id_movimento  INT           NOT NULL,
    id_carteira   INT           NOT NULL,
    data_operacao DATE          NOT NULL,
    tipo_operacao CHAR(1)       NOT NULL,
    quantidade    DECIMAL(18,8) NOT NULL CHECK (quantidade >= 0),
    PRIMARY KEY (id_movimento),
    FOREIGN KEY (id_carteira) REFERENCES carteira(id)
);

CREATE TABLE IF NOT EXISTS oraculo (
    data    DATE          NOT NULL,
    cotacao DECIMAL(18,8) NOT NULL CHECK (cotacao >= 0),
    PRIMARY KEY (data)
);

-- Usuario sugerido para a aplicacao
-- CREATE USER 'ftcoin'@'localhost' IDENTIFIED BY 'ftcoin';
-- GRANT ALL PRIVILEGES ON ftcoin.* TO 'ftcoin'@'localhost';
-- FLUSH PRIVILEGES;
