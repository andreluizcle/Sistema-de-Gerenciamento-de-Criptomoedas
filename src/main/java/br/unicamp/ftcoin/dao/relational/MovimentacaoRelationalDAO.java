package br.unicamp.ftcoin.dao.relational;

import br.unicamp.ftcoin.dao.interfaces.IMovimentacaoDAO;
import br.unicamp.ftcoin.dto.Movimentacao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do DAO de Movimentacao para o banco relacional MariaDB.
 *
 */
public class MovimentacaoRelationalDAO implements IMovimentacaoDAO {

    @Override
    public boolean inserir(Movimentacao movimentacao) {
        String sql = "INSERT INTO movimentacao (id_movimento, id_carteira, data_operacao, tipo_operacao, quantidade) " +
                "VALUES (?, ?, ?, ?, ?)";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setInt(1, movimentacao.getIdMovimento());
                ps.setInt(2, movimentacao.getIdCarteira());
                ps.setDate(3, Date.valueOf(movimentacao.getDataOperacao()));
                ps.setString(4, String.valueOf(movimentacao.getTipoOperacao()));
                ps.setBigDecimal(5, movimentacao.getQuantidade());
                return ps.executeUpdate() > 0;
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
        String sql = "SELECT id_movimento, id_carteira, data_operacao, tipo_operacao, quantidade " +
                "FROM movimentacao WHERE id_movimento = ?";
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
        String sql = "SELECT id_movimento, id_carteira, data_operacao, tipo_operacao, quantidade FROM movimentacao";
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
        String sql = "SELECT id_movimento, id_carteira, data_operacao, tipo_operacao, quantidade " +
                "FROM movimentacao WHERE id_carteira = ?";
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
        String sql = "DELETE FROM movimentacao WHERE id_carteira = ?";
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
        String sql = "SELECT COALESCE(MAX(id_movimento), 0) + 1 AS proximo FROM movimentacao";
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
        String tipoStr = rs.getString("tipo_operacao");
        char tipo = (tipoStr == null || tipoStr.isEmpty()) ? Movimentacao.COMPRA : tipoStr.charAt(0);
        return new Movimentacao(
                rs.getInt("id_movimento"),
                rs.getInt("id_carteira"),
                rs.getDate("data_operacao").toLocalDate(),
                tipo,
                rs.getBigDecimal("quantidade")
        );
    }
}
