-- Script de criacao do banco FT-Coin no MariaDB
-- Executar com privilegios de DBA.
-- Conforme o enunciado: tipo `decimal` para valores monetarios e `varchar` para textos.

CREATE DATABASE IF NOT EXISTS PooI_1s26_B03
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE PooI_1s26_B03;

CREATE TABLE CARTEIRA (
    IdCarteira INT AUTO_INCREMENT PRIMARY KEY, 
    Titular VARCHAR(50) NOT NULL, 
    Corretora VARCHAR(50) NOT NULL
);

CREATE TABLE MOVIMENTACAO (
    IdCarteira INT NOT NULL, 
    IdMovimento INT PRIMARY KEY AUTO_INCREMENT,
    Data DATE, TipoOperacao CHAR(1), 
    Quantidade DECIMAL(10,3), 
    FOREIGN KEY (IdCarteira) REFERENCES CARTEIRA(IdCarteira)
);

CREATE TABLE ORACULO (
    Data DATE NOT NULL, 
    Cotacao DECIMAL(6,2) NOT NULL
);

-- Usuario sugerido para a aplicacao
-- CREATE USER 'ftcoin'@'localhost' IDENTIFIED BY 'ftcoin';
-- GRANT ALL PRIVILEGES ON ftcoin.* TO 'ftcoin'@'localhost';
-- FLUSH PRIVILEGES;
