package br.unicamp.ftcoin.dao;

import br.unicamp.ftcoin.dto.OperationType;
import br.unicamp.ftcoin.dto.Transaction;

import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Persistencia de movimentacoes no MariaDB. */
public final class RelationalTransactionDAO implements TransactionDAO {

    /** Espelha MOVIMENTACAO.Quantidade DECIMAL(10,3). */
    private static final int QUANTITY_SCALE = 3;

    private static final String COLUMNS = "IdMovimento, IdCarteira, Data, TipoOperacao, Quantidade";

    private final DatabaseConnection database;

    public RelationalTransactionDAO(DatabaseConnection database) {
        this.database = database;
    }

    @Override
    public boolean insert(Transaction transaction) {
        String sql = "INSERT INTO MOVIMENTACAO (IdCarteira, Data, TipoOperacao, Quantidade)"
                + " VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement =
                     database.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, transaction.getWalletId());
            statement.setDate(2, java.sql.Date.valueOf(transaction.getDate()));
            statement.setString(3, String.valueOf(transaction.getType().code()));
            statement.setBigDecimal(4,
                    transaction.getQuantity().setScale(QUANTITY_SCALE, RoundingMode.HALF_UP));
            if (statement.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setId(keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            throw new PersistenceException("error.persistence.write", e);
        }
    }

    @Override
    public Transaction findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM MOVIMENTACAO WHERE IdMovimento = ?";
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
    public List<Transaction> findAll() {
        return query("SELECT " + COLUMNS + " FROM MOVIMENTACAO", null);
    }

    @Override
    public List<Transaction> findByWallet(int walletId) {
        return query("SELECT " + COLUMNS + " FROM MOVIMENTACAO WHERE IdCarteira = ?", walletId);
    }

    @Override
    public int deleteByWallet(int walletId) {
        String sql = "DELETE FROM MOVIMENTACAO WHERE IdCarteira = ?";
        try (PreparedStatement statement = database.connection().prepareStatement(sql)) {
            statement.setInt(1, walletId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("error.persistence.delete", e);
        }
    }

    private List<Transaction> query(String sql, Integer walletId) {
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement statement = database.connection().prepareStatement(sql)) {
            if (walletId != null) {
                statement.setInt(1, walletId);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    transactions.add(map(result));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw new PersistenceException("error.persistence.read", e);
        }
    }

    private Transaction map(ResultSet result) throws SQLException {
        return new Transaction(
                result.getInt("IdMovimento"),
                result.getInt("IdCarteira"),
                result.getDate("Data").toLocalDate(),
                OperationType.fromCode(result.getString("TipoOperacao").charAt(0)),
                result.getBigDecimal("Quantidade"));
    }
}
