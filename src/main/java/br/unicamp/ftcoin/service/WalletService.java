package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dao.TransactionDAO;
import br.unicamp.ftcoin.dao.WalletDAO;
import br.unicamp.ftcoin.dto.Wallet;
import br.unicamp.ftcoin.util.MessageProvider;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Regras de negocio das carteiras. */
public class WalletService {

    private final WalletDAO walletDAO;
    private final TransactionDAO transactionDAO;

    public WalletService(WalletDAO walletDAO, TransactionDAO transactionDAO) {
        this.walletDAO = walletDAO;
        this.transactionDAO = transactionDAO;
    }

    /**
     * Inclui uma carteira nova.
     *
     * @throws BusinessException se titular ou corretora estiverem em branco
     */
    public boolean add(Wallet wallet) {
        requireFilledIn(wallet);
        return walletDAO.insert(wallet);
    }

    public Wallet findById(int id) {
        return walletDAO.findById(id);
    }

    public boolean exists(int id) {
        return walletDAO.findById(id) != null;
    }

    /**
     * Atualiza titular e corretora.
     *
     * @throws BusinessException se titular ou corretora estiverem em branco
     */
    public boolean update(Wallet wallet) {
        requireFilledIn(wallet);
        if (wallet.getId() <= 0) {
            throw new BusinessException("wallet.msg.notFound");
        }
        return walletDAO.update(wallet);
    }

    /** Exclui a carteira e, antes dela, todas as suas movimentacoes. */
    public boolean delete(int id) {
        if (!exists(id)) {
            return false;
        }
        transactionDAO.deleteByWallet(id);
        return walletDAO.delete(id);
    }

    /** Relatorio 1: carteiras ordenadas por identificador. */
    public List<Wallet> listById() {
        List<Wallet> wallets = new ArrayList<>(walletDAO.findAll());
        wallets.sort(Comparator.comparingInt(Wallet::getId));
        return wallets;
    }

    /**
     * Relatorio 2: carteiras ordenadas por titular.
     *
     * <p>A ordenacao usa {@link Collator} no idioma corrente, e nao a ordem de
     * codigos do {@code String.compareTo}: so assim "Avila" vem antes de
     * "Bueno" quando o nome tem acento.</p>
     */
    public List<Wallet> listByHolder() {
        Collator collator = Collator.getInstance(MessageProvider.getInstance().locale());
        List<Wallet> wallets = new ArrayList<>(walletDAO.findAll());
        wallets.sort(Comparator.comparing(Wallet::getHolderName, collator));
        return wallets;
    }

    private void requireFilledIn(Wallet wallet) {
        if (wallet == null || isBlank(wallet.getHolderName()) || isBlank(wallet.getBrokerage())) {
            throw new BusinessException("wallet.msg.invalidData");
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
