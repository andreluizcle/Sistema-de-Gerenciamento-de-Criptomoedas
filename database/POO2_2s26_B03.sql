-- FT-Coin - estrutura do banco relacional (MariaDB).
--
-- Este script reproduz o banco POO2_2s26_B03 do professor, conferido contra o
-- servidor 143.106.243.64 (MariaDB 10.5.18) em 16/11/2026. Serve para duas
-- coisas: documentar a estrutura que a aplicacao encontra em producao e montar
-- um banco local identico para desenvolver fora da intranet da faculdade.
--
-- E fiel de proposito, inclusive onde nao e o que o grupo escreveria. Uma copia
-- local "melhorada" esconderia justamente os erros que so aparecem no servidor
-- do professor, que e onde o projeto sera avaliado. As tres diferencas que mais
-- importam estao comentadas abaixo, em cada ponto.
--
-- E idempotente: pode ser executado mais de uma vez.
--
-- Conforme o enunciado: `decimal` para quantidades e valores, `varchar` para
-- textos. Nao ha tabela ORACULO: a cotacao e obtida por consulta ao oraculo,
-- fora do banco, e mantida em cache pela aplicacao.

-- DIFERENCA 1 - a codificacao e latin1, e nao utf8mb4.
--
-- Acentos do portugues gravam e leem corretamente, porque cabem em Latin-1: um
-- byte por letra, "Assumpcao" com cedilha e til sai como E7 e E3. Ja qualquer
-- caractere fora de Latin-1 - travessao, emoji, simbolo de moeda estrangeira -
-- faz o servidor recusar o INSERT inteiro com "Incorrect string value". Nao ha
-- corrupcao silenciosa, mas tambem nao ha como gravar esses caracteres.
CREATE DATABASE IF NOT EXISTS POO2_2s26_B03
    DEFAULT CHARACTER SET latin1
    DEFAULT COLLATE latin1_swedish_ci;

USE POO2_2s26_B03;

-- Titular e Corretora sao VARCHAR(50) e a aplicacao nao confere o tamanho antes
-- de gravar: um nome mais longo chega ao servidor e volta como "Data too long
-- for column", que a tela exibe apenas como "Falha ao gravar os dados".
CREATE TABLE IF NOT EXISTS CARTEIRA (
    IdCarteira  INT AUTO_INCREMENT PRIMARY KEY,
    Titular     VARCHAR(50) NOT NULL,
    Corretora   VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- DIFERENCA 2 - a chave estrangeira vem primeiro, antes da chave primaria.
-- A ordem das colunas e a do servidor. A aplicacao nomeia todas as colunas em
-- INSERT e SELECT, entao a ordem nao a afeta; esta aqui para que um diff contra
-- o servidor nao acuse mudanca.
--
-- DIFERENCA 3 - Data, TipoOperacao e Quantidade aceitam NULL, e nao ha nenhuma
-- restricao CHECK.
--
-- O servidor nao impede quantidade negativa nem TipoOperacao fora de 'C' e 'V':
-- quem garante as duas coisas e so a aplicacao, em TransactionService. Uma
-- insercao feita por fora - outra ferramenta, um script, outro grupo - passa
-- sem reclamacao e e lida depois pelos relatorios. Acrescentar as restricoes
-- aqui deixaria a copia local mais segura que a de producao e esconderia esse
-- risco, por isso elas nao entram.
CREATE TABLE IF NOT EXISTS MOVIMENTACAO (
    IdCarteira   INT NOT NULL,
    IdMovimento  INT AUTO_INCREMENT PRIMARY KEY,
    Data         DATE,
    TipoOperacao CHAR(1),
    Quantidade   DECIMAL(10,3),
    -- A chave estrangeira fica sem nome de proposito. No servidor ela se chama
    -- MOVIMENTACAO_ibfk_1 e o indice que a acompanha se chama IdCarteira, que e
    -- o que o MariaDB gera sozinho quando a restricao nao e nomeada. Batiza-la
    -- aqui mudaria o nome do indice junto, e o schema deixaria de ser identico.
    FOREIGN KEY (IdCarteira) REFERENCES CARTEIRA (IdCarteira)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- O historico e sempre consultado por carteira, e a consulta e atendida pelo
-- indice que acompanha a chave estrangeira. Nao existe indice declarado a parte
-- no servidor, e criar um aqui so somaria um indice redundante sobre a mesma
-- coluna.
