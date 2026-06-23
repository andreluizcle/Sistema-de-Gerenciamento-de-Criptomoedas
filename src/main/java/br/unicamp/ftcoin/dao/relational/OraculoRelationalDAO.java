package br.unicamp.ftcoin.dao.relational;

import br.unicamp.ftcoin.dao.interfaces.IOraculoDAO;
import br.unicamp.ftcoin.dto.Oraculo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do DAO de Oraculo para o banco relacional MariaDB.
 *
 */
public class OraculoRelationalDAO implements IOraculoDAO {

    @Override
    public boolean inserirOuAtualizar(Oraculo oraculo) {
        String sql = "INSERT INTO ORACULO (Data, Cotacao) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE Cotacao = VALUES(Cotacao)";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(oraculo.getData()));
                ps.setBigDecimal(2, oraculo.getCotacao());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gravar cotacao: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public Oraculo consultarPorData(LocalDate data) {
        String sql = "SELECT Data, Cotacao FROM ORACULO WHERE Data = ?";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(data));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Oraculo(
                                rs.getDate("Data").toLocalDate(),
                                rs.getBigDecimal("Cotacao")
                        );
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao consultar cotacao: " + e.getMessage());
            return null;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public Oraculo consultarMaisRecente() {
        String sql = "SELECT Data, Cotacao FROM ORACULO ORDER BY Data DESC LIMIT 1";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Oraculo(
                            rs.getDate("Data").toLocalDate(),
                            rs.getBigDecimal("Cotacao")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao consultar cotacao mais recente: " + e.getMessage());
            return null;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public List<Oraculo> listarTodas() {
        String sql = "SELECT Data, Cotacao FROM ORACULO ORDER BY Data";
        List<Oraculo> resultado = new ArrayList<>();
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Oraculo(
                            rs.getDate("Data").toLocalDate(),
                            rs.getBigDecimal("Cotacao")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar cotacoes: " + e.getMessage());
        } finally {
            DatabaseConnection.fechar(conexao);
        }
        return resultado;
    }
}
