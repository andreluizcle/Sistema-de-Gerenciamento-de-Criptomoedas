package br.unicamp.ftcoin.dao;

import br.unicamp.ftcoin.dto.Wallet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistencia de carteiras em memoria, sobre colecoes Java.
 *
 * <p>Guarda e devolve copias. Sem isso o mapa seria apenas um apelido para os
 * objetos do chamador: editar uma carteira obtida por {@code findById} ja
 * alteraria o "banco" antes de qualquer {@code update}, e a alteracao
 * permaneceria mesmo se a validacao reprovasse a edicao.</p>
 */
public final class MemoryWalletDAO implements WalletDAO {

    private final Map<Integer, Wallet> wallets = new LinkedHashMap<>();

    /** Simula o AUTO_INCREMENT da coluna IdCarteira. */
    private int sequence = 0;

    @Override
    public boolean insert(Wallet wallet) {
        if (wallet == null) {
            return false;
        }
        wallet.setId(++sequence);
        wallets.put(wallet.getId(), wallet.copy());
        return true;
    }

    @Override
    public Wallet findById(int id) {
        Wallet stored = wallets.get(id);
        return stored == null ? null : stored.copy();
    }

    @Override
    public boolean update(Wallet wallet) {
        if (wallet == null || !wallets.containsKey(wallet.getId())) {
            return false;
        }
        wallets.put(wallet.getId(), wallet.copy());
        return true;
    }

    @Override
    public boolean delete(int id) {
        return wallets.remove(id) != null;
    }

    @Override
    public List<Wallet> findAll() {
        List<Wallet> copies = new ArrayList<>(wallets.size());
        for (Wallet stored : wallets.values()) {
            copies.add(stored.copy());
        }
        return copies;
    }
}
