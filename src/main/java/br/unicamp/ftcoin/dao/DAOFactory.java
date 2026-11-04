package br.unicamp.ftcoin.dao;

/**
 * Fabrica abstrata dos objetos de acesso a dados.
 *
 * <p>Concentra em um unico ponto a escolha da persistencia. O resto do sistema
 * recebe os contratos e nunca menciona uma implementacao concreta, de modo que
 * trocar memoria por MariaDB nao altera servico, controlador nem tela.</p>
 *
 * <p>E {@link AutoCloseable} para que a liberacao de recursos seja responsabilidade
 * de quem abriu: um {@code try}-com-recursos na partida garante que a conexao
 * feche na saida, inclusive em caso de erro.</p>
 */
public sealed interface DAOFactory extends AutoCloseable permits MemoryDAOFactory, RelationalDAOFactory {

    WalletDAO walletDAO();

    TransactionDAO transactionDAO();

    /** Redeclarado sem excecao checada para manter o try-com-recursos limpo. */
    @Override
    void close();

    /** Unica porta de entrada da fabrica. */
    static DAOFactory of(PersistenceType type) {
        return switch (type) {
            case MEMORY -> new MemoryDAOFactory();
            case RELATIONAL -> new RelationalDAOFactory();
        };
    }
}
