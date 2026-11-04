package br.unicamp.ftcoin.dao;

/**
 * Fabrica da persistencia relacional.
 *
 * <p>E o unico lugar do sistema que menciona o Singleton da conexao: os DAOs a
 * recebem pelo construtor e seguem testaveis e substituiveis.</p>
 */
public final class RelationalDAOFactory implements DAOFactory {

    private final DatabaseConnection database = DatabaseConnection.getInstance();
    private final WalletDAO walletDAO = new RelationalWalletDAO(database);
    private final TransactionDAO transactionDAO = new RelationalTransactionDAO(database);

    @Override
    public WalletDAO walletDAO() {
        return walletDAO;
    }

    @Override
    public TransactionDAO transactionDAO() {
        return transactionDAO;
    }

    /** Fecha a conexao compartilhada ao fim da sessao. */
    @Override
    public void close() {
        database.close();
    }

    /** URL em uso, sem credenciais, para a mensagem de partida. */
    public String databaseUrl() {
        return database.publicUrl();
    }
}
