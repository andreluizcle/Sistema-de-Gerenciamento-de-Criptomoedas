package br.unicamp.ftcoin.dao;

import br.unicamp.ftcoin.util.FtCoinException;

/**
 * Falha ao ler ou gravar dados.
 *
 * <p>Nao e checada de proposito: obrigar {@code throws SQLException} em todas as
 * assinaturas de DAO e servico foi justamente o que levou a versao anterior a
 * escrever {@code catch (SQLException e) { return false; }}, engolindo o erro e
 * fazendo um banco fora do ar parecer um banco vazio.</p>
 */
public class PersistenceException extends FtCoinException {

    private static final long serialVersionUID = 1L;

    public PersistenceException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }
}
