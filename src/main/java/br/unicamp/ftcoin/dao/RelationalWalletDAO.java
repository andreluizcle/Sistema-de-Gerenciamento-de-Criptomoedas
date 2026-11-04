package br.unicamp.ftcoin.dao;

import br.unicamp.ftcoin.dto.Wallet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia de carteiras no MariaDB.
 *
 * <p>Recebe o {@link DatabaseConnection} e pede a conexao a cada metodo, em vez
 * de guardar uma {@code Connection} crua: assim uma queda de rede e recuperada
 * na proxima chamada, e ainda assim ha uma unica conexao por sessao.</p>
 */
public final class RelationalWalletDAO implements WalletDAO {

    private final DatabaseConnection database;

    public RelationalWalletDAO(DatabaseConnection database) {
        this.database = database;
    }

    @Override
    public boolean insert(Wallet wallet) {
        String sql = "INSERT INTO CARTEIRA (Titular, Corretora) VALUES (?, ?)";
        try (PreparedStatement statement =
                     database.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, wallet.getHolderName());
            statement.setString(2, wallet.getBrokerage());
            if (statement.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    wallet.setId(keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            throw new PersistenceException("error.persistence.write", e);
        }
    }

    @Override
    public Wallet findById(int id) {
        String sql = "SELECT IdCarteira, Titular, Corretora FROM CARTEIRA WHERE IdCarteira = ?";
        try (PreparedStatement statement = database.connection().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("error.persistence.read", e);
        }
    }

    @Override
    public boolean update(Wallet wallet) {
        String sql = "UPDATE CARTEIRA SET Titular = ?, Corretora = ? WHERE IdCarteira = ?";
        try (PreparedStatement statement = database.connection().prepareStatement(sql)) {
            statement.setString(1, wallet.getHolderName());
            statement.setString(2, wallet.getBrokerage());
            statement.setInt(3, wallet.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenceException("error.persistence.write", e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM CARTEIRA WHERE IdCarteira = ?";
        try (PreparedStatement statement = database.connection().prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenceException("error.persistence.delete", e);
        }
    }

    @Override
    public List<Wallet> findAll() {
        String sql = "SELECT IdCarteira, Titular, Corretora FROM CARTEIRA";
        List<Wallet> wallets = new ArrayList<>();
        try (PreparedStatement statement = database.connection().prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                wallets.add(map(result));
            }
            return wallets;
        } catch (SQLException e) {
            throw new PersistenceException("error.persistence.read", e);
        }
    }

    private Wallet map(ResultSet result) throws SQLException {
        return new Wallet(
                result.getInt("IdCarteira"),
                result.getString("Titular"),
                result.getString("Corretora"));
    }
}
