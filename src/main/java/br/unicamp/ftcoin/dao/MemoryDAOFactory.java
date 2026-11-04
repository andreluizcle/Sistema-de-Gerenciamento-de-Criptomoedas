package br.unicamp.ftcoin.dao;

/**
 * Fabrica da persistencia em memoria.
 *
 * <p>Os dois DAOs sao criados uma vez e compartilhados: em memoria eles <i>sao</i>
 * o banco, e uma segunda instancia seria um segundo banco, vazio.</p>
 */
public final class MemoryDAOFactory implements DAOFactory {

    private final WalletDAO walletDAO = new MemoryWalletDAO();
    private final TransactionDAO transactionDAO = new MemoryTransactionDAO();

    @Override
    public WalletDAO walletDAO() {
        return walletDAO;
    }

    @Override
    public TransactionDAO transactionDAO() {
        return transactionDAO;
    }

    /** Nada a liberar: o banco simulado sao os mapas dentro dos DAOs. */
    @Override
    public void close() {
    }
}
