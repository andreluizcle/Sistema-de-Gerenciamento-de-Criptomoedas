package br.unicamp.ftcoin.dao.relational;

import br.unicamp.ftcoin.dao.interfaces.ICarteiraDAO;
import br.unicamp.ftcoin.dto.Carteira;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do DAO de Carteira para o banco relacional MariaDB.
 *
 */
public class CarteiraRelationalDAO implements ICarteiraDAO {

    @Override
    public boolean inserir(Carteira carteira) {
        // IdCarteira é AUTO_INCREMENT — não incluímos na lista de colunas
        String sql = "INSERT INTO CARTEIRA (Titular, Corretora) VALUES (?, ?)";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            // RETURN_GENERATED_KEYS solicita ao driver JDBC que retorne
            // as chaves geradas automaticamente pelo AUTO_INCREMENT
            try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, carteira.getNomeTitular());
                ps.setString(2, carteira.getCorretora());
                int linhas = ps.executeUpdate();
                if (linhas > 0) {
                    // Recupera o ID gerado pelo MariaDB e atualiza o objeto em memória
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            carteira.setId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir carteira: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public Carteira consultar(int id) {
        String sql = "SELECT IdCarteira, Titular, Corretora FROM CARTEIRA WHERE IdCarteira = ?";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Carteira(
                                rs.getInt("IdCarteira"),
                                rs.getString("Titular"),
                                rs.getString("Corretora")
                        );
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao consultar carteira: " + e.getMessage());
            return null;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public boolean atualizar(Carteira carteira) {
        String sql = "UPDATE CARTEIRA SET Titular = ?, Corretora = ? WHERE IdCarteira = ?";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setString(1, carteira.getNomeTitular());
                ps.setString(2, carteira.getCorretora());
                ps.setInt(3, carteira.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar carteira: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public boolean excluir(int id) {
        String sql = "DELETE FROM CARTEIRA WHERE IdCarteira = ?";
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir carteira: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.fechar(conexao);
        }
    }

    @Override
    public List<Carteira> listarTodas() {
        String sql = "SELECT IdCarteira, Titular, Corretora FROM CARTEIRA";
        List<Carteira> resultado = new ArrayList<>();
        Connection conexao = null;
        try {
            conexao = DatabaseConnection.abrir();
            try (PreparedStatement ps = conexao.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Carteira(
                            rs.getInt("IdCarteira"),
                            rs.getString("Titular"),
                            rs.getString("Corretora")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar carteiras: " + e.getMessage());
        } finally {
            DatabaseConnection.fechar(conexao);
        }
        return resultado;
    }

    @Override
    public boolean existe(int id) {
        return consultar(id) != null;
    }
}
