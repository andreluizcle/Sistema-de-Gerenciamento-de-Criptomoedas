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
    private static final LocalDate DAY_3 = LocalDate.of(2026, 1, 3);

    private TransactionDAO transactionDAO;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        StubQuoteSource source = new StubQuoteSource()
                .put(DAY_1, "10.00")
                .put(DAY_2, "20.00")
                .put(DAY_3, "30.00");
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

    @Test
    @DisplayName("Venda com data anterior a compra e recusada e nada e gravado")
    void rejectsSaleDatedBeforeThePurchase() {
        service.buy(WALLET, new BigDecimal("10"), DAY_2);

        // O saldo total e 10, mas na data da venda ainda era zero. Antes da
        // correcao esta venda era aceita e qualquer relatorio da carteira
        // passava a terminar em rastro de pilha.
        assertThrows(BusinessException.class,
                () -> service.sell(WALLET, new BigDecimal("10"), DAY_1));
        assertEquals(1, transactionDAO.findAll().size(), "apenas a compra deveria existir");
    }

    @Test
    @DisplayName("Venda retroativa que inviabilizaria um dia posterior e recusada")
    void rejectsSaleThatWouldBreakALaterDay() {
        service.buy(WALLET, new BigDecimal("10"), DAY_1);
        service.sell(WALLET, new BigDecimal("10"), DAY_3);

        // Esta venda cabe no saldo da sua propria data (10 no dia 1) e cabe no
        // saldo total depois dela, mas deixaria o dia 3 negativo. E o caso que
        // obriga a validacao a simular a serie inteira, e nao um dia isolado.
        assertThrows(BusinessException.class,
                () -> service.sell(WALLET, new BigDecimal("5"), DAY_2));
        assertEquals(2, transactionDAO.findAll().size(), "a terceira operacao nao deveria entrar");
    }

    @Test
    @DisplayName("Quantidade que arredonda para zero e recusada e nada e gravado")
    void rejectsQuantityThatRoundsToZero() {
        // A coluna guarda tres casas. Antes da correcao, 0,0001 passava na
        // checagem de sinal, virava 0,000 no arredondamento e era gravada com
        // recibo de sucesso e valor R$ 0,00.
        assertThrows(BusinessException.class,
                () -> service.buy(WALLET, new BigDecimal("0.0001"), DAY_1));
        assertEquals(0, transactionDAO.findAll().size(), "nada deveria ter sido gravado");
    }

    @Test
    @DisplayName("Quantidade que nao cabe na coluna e recusada")
    void rejectsQuantityBeyondTheColumn() {
        // DECIMAL(10,3) comporta ate 9999999,999.
        assertThrows(BusinessException.class,
                () -> service.buy(WALLET, new BigDecimal("10000000"), DAY_1));
        // Arredonda para 10000000,000 e tambem nao cabe.
        assertThrows(BusinessException.class,
                () -> service.buy(WALLET, new BigDecimal("9999999.9996"), DAY_1));
        // Expoente gigante: recusado pela contagem de digitos, sem materializar
        // o numero. Sem essa guarda o setScale tentava um inteiro de um bilhao
        // de digitos e derrubava o programa.
        assertThrows(BusinessException.class,
                () -> service.buy(WALLET, new BigDecimal("1e999999999"), DAY_1));
        assertEquals(0, transactionDAO.findAll().size(), "nada deveria ter sido gravado");

        service.buy(WALLET, new BigDecimal("9999999.999"), DAY_1);
        assertEquals(1, transactionDAO.findAll().size(), "o maximo da coluna deveria ser aceito");
    }

    @Test
    @DisplayName("Data fora do intervalo aceito e recusada e nada e gravado")
    void rejectsDateOutsideTheAcceptedRange() {
        assertThrows(BusinessException.class,
                () -> service.buy(WALLET, BigDecimal.ONE, TransactionService.MIN_DATE.minusDays(1)));
        // Sem o teto, uma data em ano distante entrava na base e o relatorio
        // tentava montar uma cotacao por dia do intervalo.
        assertThrows(BusinessException.class,
                () -> service.buy(WALLET, BigDecimal.ONE, TransactionService.MAX_DATE.plusDays(1)));
        assertThrows(BusinessException.class,
                () -> service.buy(WALLET, BigDecimal.ONE, LocalDate.MAX));
        assertEquals(0, transactionDAO.findAll().size(), "nada deveria ter sido gravado");
    }
}
