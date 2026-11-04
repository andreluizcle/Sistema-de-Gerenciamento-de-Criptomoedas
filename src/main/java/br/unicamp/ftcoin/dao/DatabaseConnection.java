package br.unicamp.ftcoin.dao;

import br.unicamp.ftcoin.util.EnvLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexao unica com o MariaDB.
 *
 * <p>Singleton porque abrir e fechar uma conexao a cada operacao — como fazia a
 * versao anterior — custa uma ida a rede por consulta, e um relatorio de ganho e
 * perda chega a dezenas delas. Aqui a conexao e aberta na primeira chamada e
 * reaproveitada pela sessao inteira.</p>
 *
 * <p>Nenhuma credencial esta no codigo: tudo vem de variaveis de ambiente ou do
 * arquivo {@code .env}, via {@link EnvLoader}. Veja {@code env.example}.</p>
 */
public final class DatabaseConnection {

    private static DatabaseConnection instance;

    private Connection connection;

    private DatabaseConnection() {
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * A conexao reaproveitada. Abre na primeira chamada e reabre se o servidor
     * tiver derrubado a anterior — a rede da faculdade cai, e uma conexao
     * guardada para sempre morreria junto.
     *
     * @throws PersistenceException se nao for possivel conectar
     */
    public synchronized Connection connection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = open();
            }
            return connection;
        } catch (SQLException e) {
            throw new PersistenceException("error.persistence.connect", e);
        }
    }

    private Connection open() throws SQLException {
        String user = EnvLoader.get("DB_USER");
        String password = EnvLoader.get("DB_PASSWORD");
        if (user == null || password == null) {
            throw new SQLException("DB_USER and DB_PASSWORD are not configured");
        }
        return DriverManager.getConnection(url(), user, password);
    }

    private String url() {
        String host = EnvLoader.get("DB_HOST", "localhost");
        String port = EnvLoader.get("DB_PORT", "3306");
        String database = EnvLoader.get("DB_NAME", "ftcoin");
        return "jdbc:mariadb://" + host + ":" + port + "/" + database;
    }

    /** URL sem credenciais, para exibir na partida. */
    public String publicUrl() {
        return url();
    }

    /** Fecha a conexao, se houver. Chamado pela fabrica relacional. */
    public synchronized void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
            // A conexao ja estava invalida; nao ha nada a fazer nem a informar.
        } finally {
            connection = null;
        }
    }
}
