-- FT-Coin - estrutura do banco relacional (MariaDB).
--
-- O banco de producao e o fornecido pelo professor (POO2_2s26_B03, acessivel
-- apenas pela intranet da faculdade). Este script existe para documentar a
-- estrutura esperada e para montar um banco local identico durante o
-- desenvolvimento. E idempotente: pode ser executado mais de uma vez.
--
-- Conforme o enunciado: `decimal` para quantidades e valores, `varchar` para
-- textos. Nao ha tabela ORACULO: a cotacao e obtida por consulta ao oraculo,
-- fora do banco, e mantida em cache pela aplicacao.

CREATE DATABASE IF NOT EXISTS POO2_2s26_B03
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE POO2_2s26_B03;

CREATE TABLE IF NOT EXISTS CARTEIRA (
    IdCarteira  INT AUTO_INCREMENT PRIMARY KEY,
    Titular     VARCHAR(50) NOT NULL,
    Corretora   VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS MOVIMENTACAO (
    IdMovimento  INT AUTO_INCREMENT PRIMARY KEY,
    IdCarteira   INT         NOT NULL,
    Data         DATE        NOT NULL,
    TipoOperacao CHAR(1)     NOT NULL,
    Quantidade   DECIMAL(10,3) NOT NULL,
    CONSTRAINT fk_movimentacao_carteira
        FOREIGN KEY (IdCarteira) REFERENCES CARTEIRA(IdCarteira),
    -- O enunciado exige quantidade nao negativa; sem a restricao isso dependeria
    -- apenas da validacao da aplicacao.
    CONSTRAINT ck_movimentacao_quantidade CHECK (Quantidade >= 0),
    CONSTRAINT ck_movimentacao_tipo CHECK (TipoOperacao IN ('C', 'V'))
);

-- O historico e sempre consultado por carteira.
CREATE INDEX idx_movimentacao_carteira ON MOVIMENTACAO (IdCarteira);
