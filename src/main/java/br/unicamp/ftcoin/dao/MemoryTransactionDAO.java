package br.unicamp.ftcoin.dao;

import br.unicamp.ftcoin.dto.Transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistencia de movimentacoes em memoria, sobre colecoes Java.
 *
 * <p>Guarda e devolve copias, pela mesma razao de {@link MemoryWalletDAO}.</p>
 */
public final class MemoryTransactionDAO implements TransactionDAO {

    private final Map<Integer, Transaction> transactions = new LinkedHashMap<>();

    /** Simula o AUTO_INCREMENT da coluna IdMovimento. */
    private int sequence = 0;

    @Override
    public boolean insert(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        transaction.setId(++sequence);
        transactions.put(transaction.getId(), transaction.copy());
        return true;
    }

    @Override
    public Transaction findById(int id) {
        Transaction stored = transactions.get(id);
        return stored == null ? null : stored.copy();
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> copies = new ArrayList<>(transactions.size());
        for (Transaction stored : transactions.values()) {
            copies.add(stored.copy());
        }
        return copies;
    }

    @Override
    public List<Transaction> findByWallet(int walletId) {
        List<Transaction> found = new ArrayList<>();
        for (Transaction stored : transactions.values()) {
            if (stored.getWalletId() == walletId) {
                found.add(stored.copy());
            }
        }
        return found;
    }

    @Override
    public int deleteByWallet(int walletId) {
        int removed = 0;
        var iterator = transactions.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getWalletId() == walletId) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }
}
