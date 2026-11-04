package br.unicamp.ftcoin.dao;

import br.unicamp.ftcoin.dto.Transaction;

import java.util.List;

/**
 * Contrato de persistencia de movimentacoes.
 *
 * <p>Selada pelas mesmas razoes de {@link WalletDAO}.</p>
 */
public sealed interface TransactionDAO permits MemoryTransactionDAO, RelationalTransactionDAO {

    /** Insere a movimentacao e atribui a ela o identificador gerado. */
    boolean insert(Transaction transaction);

    /** Movimentacao do identificador, ou {@code null} se nao existir. */
    Transaction findById(int id);

    List<Transaction> findAll();

    List<Transaction> findByWallet(int walletId);

    /** Remove as movimentacoes da carteira e devolve quantas foram removidas. */
    int deleteByWallet(int walletId);
}
