package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dao.TransactionDAO;
import br.unicamp.ftcoin.dto.OperationType;
import br.unicamp.ftcoin.dto.Quote;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.dto.TransactionReceipt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Regras de negocio das compras e vendas. */
public class TransactionService {

    /** Espelha MOVIMENTACAO.Quantidade DECIMAL(10,3). */
    public static final int QUANTITY_SCALE = 3;

    private final TransactionDAO transactionDAO;
    private final OracleService oracleService;

    public TransactionService(TransactionDAO transactionDAO, OracleService oracleService) {
        this.transactionDAO = transactionDAO;
        this.oracleService = oracleService;
    }

    /**
     * Registra uma compra.
     *
     * @throws BusinessException se a quantidade nao for positiva
     */
    public TransactionReceipt buy(int walletId, BigDecimal quantity, LocalDate date) {
        return register(walletId, quantity, date, OperationType.BUY);
    }

    /**
     * Registra uma venda.
     *
     * @throws BusinessException se a quantidade nao for positiva ou exceder o saldo
     */
    public TransactionReceipt sell(int walletId, BigDecimal quantity, LocalDate date) {
        BigDecimal normalized = normalize(quantity);
        if (normalized.compareTo(balanceOf(walletId)) > 0) {
            throw new BusinessException("transaction.msg.insufficientBalance");
        }
        return register(walletId, normalized, date, OperationType.SELL);
    }

    private TransactionReceipt register(int walletId, BigDecimal quantity,
                                        LocalDate date, OperationType type) {
        BigDecimal normalized = normalize(quantity);
        Transaction transaction = new Transaction(0, walletId, date, type, normalized);
        if (!transactionDAO.insert(transaction)) {
            throw new BusinessException("transaction.msg.notRegistered");
        }
        Quote quote = oracleService.quoteOn(date);
        return new TransactionReceipt(transaction, quote, balanceOf(walletId));
    }

    /** Cotacao de uma data, para a opcao "cotar moeda virtual" do menu. */
    public Quote quoteOn(LocalDate date) {
        return oracleService.quoteOn(date);
    }

    /** Saldo em moeda virtual: compras menos vendas. */
    public BigDecimal balanceOf(int walletId) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction transaction : transactionDAO.findByWallet(walletId)) {
            balance = transaction.isBuy()
                    ? balance.add(transaction.getQuantity())
                    : balance.subtract(transaction.getQuantity());
        }
        return balance;
    }

    /** Relatorio 4: historico da carteira, em ordem cronologica. */
    public List<Transaction> historyOf(int walletId) {
        List<Transaction> history = new ArrayList<>(transactionDAO.findByWallet(walletId));
        history.sort(Comparator.comparing(Transaction::getDate).thenComparingInt(Transaction::getId));
        return history;
    }

    private BigDecimal normalize(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessException("transaction.msg.invalidQuantity");
        }
        return quantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
    }
}
