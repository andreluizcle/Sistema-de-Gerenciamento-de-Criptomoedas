package br.unicamp.ftcoin.dao.relational;

import br.unicamp.ftcoin.dao.interfaces.IMovimentacaoDAO;
import br.unicamp.ftcoin.dto.Movimentacao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do DAO de Movimentacao para o banco relacional MariaDB.
 *
 */
public class MovimentacaoRelationalDAO implements IMovimentacaoDAO {

    @Override
    public boolean inserir(Movimentacao movimentacao) {
        // IdMovimento é AUTO_INCREMENT — não incluímos na lista de colunas
        String sql = "INSERT INTO MOVIMENTACAO (IdCarteira, Data, TipoOperacao, Quantidade) " +
                "VALUES (?, ?, ?, ?)";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            // RETURN_GENERATED_KEYS solicita ao driver JDBC que retorne
            // as chaves geradas automaticamente pelo AUTO_INCREMENT
            try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, movimentacao.getIdCarteira());
                ps.setDate(2, Date.valueOf(movimentacao.getDataOperacao()));
                ps.setString(3, String.valueOf(movimentacao.getTipoOperacao()));
                ps.setBigDecimal(4, movimentacao.getQuantidade());
                int linhas = ps.executeUpdate();
                if (linhas > 0) {
                    // Recupera o ID gerado pelo MariaDB e atualiza o objeto em memória
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            movimentacao.setIdMovimento(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir movimentacao: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public Movimentacao consultar(int idMovimento) {
        String sql = "SELECT IdMovimento, IdCarteira, Data, TipoOperacao, Quantidade " +
                "FROM MOVIMENTACAO WHERE IdMovimento = ?";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setInt(1, idMovimento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapear(rs);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao consultar movimentacao: " + e.getMessage());
            return null;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public List<Movimentacao> listarTodas() {
        String sql = "SELECT IdMovimento, IdCarteira, Data, TipoOperacao, Quantidade FROM MOVIMENTACAO";
        List<Movimentacao> resultado = new ArrayList<>();
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar movimentacoes: " + e.getMessage());
        } finally {
            DatabaseConnection.fechar(conexao);
        }
        return resultado;
    }

    @Override
    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        String sql = "SELECT IdMovimento, IdCarteira, Data, TipoOperacao, Quantidade " +
                "FROM MOVIMENTACAO WHERE IdCarteira = ?";
        List<Movimentacao> resultado = new ArrayList<>();
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setInt(1, idCarteira);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        resultado.add(mapear(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar movimentacoes da carteira: " + e.getMessage());
        } finally {
            DatabaseConnection.fechar(conexao);
        }
        return resultado;
    }

    @Override
    public boolean excluirPorCarteira(int idCarteira) {
        String sql = "DELETE FROM MOVIMENTACAO WHERE IdCarteira = ?";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setInt(1, idCarteira);
                return ps.executeUpdate() >= 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir movimentacoes da carteira: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public int proximoId() {
        // Obsoleto com AUTO_INCREMENT — mantido para compatibilidade com
        // a interface IMovimentacaoDAO e a implementação em memória.
        // Na implementação relacional, o ID é gerado pelo banco.
        String sql = "SELECT COALESCE(MAX(IdMovimento), 0) + 1 AS proximo FROM MOVIMENTACAO";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("proximo");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar proximo id: " + e.getMessage());
        } finally {
            DatabaseConnection.fechar(conexao);
        }
        return 1;
    }

    private Movimentacao mapear(ResultSet rs) throws SQLException {
        String tipoStr = rs.getString("TipoOperacao");
        char tipo = (tipoStr == null || tipoStr.isEmpty()) ? Movimentacao.COMPRA : tipoStr.charAt(0);
        return new Movimentacao(
                rs.getInt("IdMovimento"),
                rs.getInt("IdCarteira"),
                rs.getDate("Data").toLocalDate(),
                tipo,
                rs.getBigDecimal("Quantidade")
        );
    }
}
