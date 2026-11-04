package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dao.MemoryTransactionDAO;
import br.unicamp.ftcoin.dao.TransactionDAO;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.dto.TransactionReceipt;
import br.unicamp.ftcoin.oracle.StubQuoteSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Regras de negocio das compras e vendas. */
class TransactionServiceTest {

    private static final int WALLET = 1;
    private static final LocalDate DAY_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2026, 1, 2);

    private TransactionDAO transactionDAO;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        StubQuoteSource source = new StubQuoteSource()
                .put(DAY_1, "10.00")
                .put(DAY_2, "20.00");
        transactionDAO = new MemoryTransactionDAO();
        service = new TransactionService(transactionDAO, new OracleService(source));
    }

    @Test
    @DisplayName("Quantidade nula, zero ou negativa e recusada e nada e gravado")
    void rejectsInvalidQuantity() {
        assertThrows(BusinessException.class, () -> service.buy(WALLET, null, DAY_1));
        assertThrows(BusinessException.class, () -> service.buy(WALLET, BigDecimal.ZERO, DAY_1));
        assertThrows(BusinessException.class,
                () -> service.buy(WALLET, new BigDecimal("-5"), DAY_1));
        assertEquals(0, transactionDAO.findAll().size(), "nada deveria ter sido gravado");
    }

    @Test
    @DisplayName("Venda acima do saldo e recusada e nada e gravado")
    void rejectsSaleBeyondBalance() {
        service.buy(WALLET, new BigDecimal("100"), DAY_1);

        assertThrows(BusinessException.class,
                () -> service.sell(WALLET, new BigDecimal("100.001"), DAY_1));
        assertEquals(1, transactionDAO.findAll().size(), "apenas a compra deveria existir");
    }

    @Test
    @DisplayName("Vender exatamente o saldo e permitido e zera a carteira")
    void allowsSellingTheWholeBalance() {
        service.buy(WALLET, new BigDecimal("100"), DAY_1);

        TransactionReceipt receipt = service.sell(WALLET, new BigDecimal("100"), DAY_2);

        assertEquals(0, receipt.newBalance().signum(), "saldo apos a venda");
    }

    @Test
    @DisplayName("O saldo acompanha compras e vendas")
    void tracksTheBalance() {
        service.buy(WALLET, new BigDecimal("100"), DAY_1);
        service.buy(WALLET, new BigDecimal("50"), DAY_2);
        service.sell(WALLET, new BigDecimal("30"), DAY_2);

        assertEquals(0, service.balanceOf(WALLET).compareTo(new BigDecimal("120")));
        assertEquals(0, service.balanceOf(99).signum(), "carteira sem movimentacao");
    }

    @Test
    @DisplayName("A quantidade e gravada com tres casas, como a coluna do banco")
    void storesQuantityWithThreeDecimals() {
        service.buy(WALLET, new BigDecimal("1.23456"), DAY_1);

        assertEquals(new BigDecimal("1.235"), transactionDAO.findAll().get(0).getQuantity());
    }

    @Test
    @DisplayName("O comprovante traz a cotacao da data da operacao, nao a de hoje")
    void receiptCarriesTheQuoteOfTheTransactionDate() {
        TransactionReceipt receipt = service.buy(WALLET, new BigDecimal("10"), DAY_1);

        assertEquals(DAY_1, receipt.quote().date(), "data da cotacao");
        assertEquals(new BigDecimal("10.00"), receipt.quote().price(), "cotacao");
        assertEquals(0, receipt.amount().compareTo(new BigDecimal("100.00")), "valor da operacao");
    }

    @Test
    @DisplayName("O historico sai em ordem cronologica")
    void historyIsSortedByDate() {
        service.buy(WALLET, new BigDecimal("10"), DAY_2);
        service.buy(WALLET, new BigDecimal("10"), DAY_1);

        List<Transaction> history = service.historyOf(WALLET);

        assertEquals(List.of(DAY_1, DAY_2), history.stream().map(Transaction::getDate).toList());
    }
}
