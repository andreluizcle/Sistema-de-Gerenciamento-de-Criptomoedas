package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dao.MemoryTransactionDAO;
import br.unicamp.ftcoin.dao.TransactionDAO;
import br.unicamp.ftcoin.dto.OperationType;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.dto.WalletResult;
import br.unicamp.ftcoin.oracle.StubQuoteSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Apuracao: o que ela pede ao oraculo. */
class ReportServiceTest {

    private static final int WALLET = 1;
    private static final LocalDate FIRST = LocalDate.of(2026, 3, 1);
    private static final LocalDate MIDDLE = LocalDate.of(2026, 3, 5);
    private static final LocalDate LAST = LocalDate.of(2026, 3, 10);

    private StubQuoteSource source;
    private TransactionDAO transactionDAO;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        source = new StubQuoteSource()
                .put(FIRST, "10.00")
                .put(MIDDLE, "12.00")
                .put(LAST, "15.00")
                .put(LocalDate.now(), "20.00");
        transactionDAO = new MemoryTransactionDAO();
        OracleService oracleService = new OracleService(source);
        TransactionService transactionService = new TransactionService(transactionDAO, oracleService);
        reportService = new ReportService(transactionService, oracleService);
    }

    private void record(LocalDate date, OperationType type, String quantity) {
        transactionDAO.insert(new Transaction(0, WALLET, date, type, new BigDecimal(quantity)));
    }

    @Test
    @DisplayName("Apurar uma carteira faz uma unica consulta por intervalo ao oraculo")
    void prewarmsTheWalletSpanInOneCall() {
        // Sem o aquecimento previo, a apuracao pediria uma cotacao por data do
        // historico — com o oraculo em rede, uma ida a rede por movimentacao.
        record(FIRST, OperationType.BUY, "100");
        record(MIDDLE, OperationType.BUY, "100");
        record(LAST, OperationType.SELL, "50");

        WalletResult result = reportService.resultOf(WALLET);

        assertEquals(1, source.rangeCount(), "uma consulta por intervalo");
        assertEquals(1, source.fetchCount(),
                "apenas a data de valoracao e buscada a parte, fora do intervalo");
        assertEquals(0, result.balance().compareTo(new BigDecimal("150")), "saldo apurado");
    }

    @Test
    @DisplayName("Carteira sem movimentacao nao consulta intervalo algum")
    void doesNotWarmUpAnEmptyWallet() {
        WalletResult result = reportService.resultOf(WALLET);

        assertEquals(0, source.rangeCount(), "nao ha intervalo a aquecer");
        assertTrue(result.days().isEmpty(), "serie diaria vazia");
    }

    @Test
    @DisplayName("Apurar a mesma carteira de novo aproveita o cache")
    void secondReportReusesTheCache() {
        record(FIRST, OperationType.BUY, "100");
        record(LAST, OperationType.SELL, "40");

        reportService.resultOf(WALLET);
        reportService.resultOf(WALLET);

        assertEquals(1, source.rangeCount(), "o segundo relatorio nao volta ao oraculo");
        assertEquals(1, source.fetchCount(), "a data de valoracao tambem fica em cache");
    }
}
