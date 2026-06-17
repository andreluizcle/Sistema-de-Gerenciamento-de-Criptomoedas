package br.unicamp.ftcoin.dao.relational;

import br.unicamp.ftcoin.util.EnvLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilitario para obtencao e fechamento da conexao com o banco
 * relacional MariaDB.
 *
 * Todas as credenciais sao lidas de variaveis de ambiente ou do
 * arquivo .env (via EnvLoader). Nenhuma informacao sensivel esta
 * hardcoded neste arquivo - ele e seguro para versionamento.
 *
 * Chaves esperadas no .env / variaveis de ambiente:
 *   DB_HOST      - endereco do servidor (ex.: 143.106.243.64)
 *   DB_PORT      - porta (ex.: 3306)
 *   DB_NAME      - nome do banco (ex.: CashFlow2026)
 *   DB_USER      - usuario
 *   DB_PASSWORD  - senha
 *
 * Veja .env.example na raiz do projeto para um modelo.
 */
public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    /** Monta a URL JDBC a partir das variaveis de ambiente / .env. */
    private static String montarUrl() {
        String host = EnvLoader.get("DB_HOST", "localhost");
        String porta = EnvLoader.get("DB_PORT", "3306");
        String nome = EnvLoader.get("DB_NAME", "ftcoin");
        return "jdbc:mariadb://" + host + ":" + porta + "/" + nome;
    }

    /** Abre uma conexao com o banco. Lanca SQLException se faltar configuracao. */
    public static Connection abrir() throws SQLException {
        String url = montarUrl();
        String usuario = EnvLoader.get("DB_USER");
        String senha = EnvLoader.get("DB_PASSWORD");

        if (usuario == null || senha == null) {
            throw new SQLException(
                "Credenciais nao configuradas. " +
                "Defina DB_USER e DB_PASSWORD no arquivo .env ou nas variaveis de ambiente."
            );
        }
        return DriverManager.getConnection(url, usuario, senha);
    }

    public static void fechar(Connection conexao) {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (SQLException ignorada) {
                // sem acao - conexao ja estava em estado invalido
            }
        }
    }

    /** Retorna a URL JDBC montada (uso em logs / mensagens). Nao expoe senha. */
    public static String getUrlPublica() {
        return montarUrl();
    }
}
