package br.unicamp.ftcoin.util;

import br.unicamp.ftcoin.service.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Registro de execucao: arquivo, metricas e seguranca em caso de falha. */
class ActivityLogTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 1);

    @TempDir
    Path tempDir;

    private final ActivityLog log = ActivityLog.getInstance();

    /** O registro e um Singleton compartilhado: cada teste comeca do zero. */
    @BeforeEach
    void resetBefore() {
        log.endSession();
    }

    @AfterEach
    void resetAfter() {
        log.endSession();
        MessageProvider.getInstance().setLocale(Locale.forLanguageTag("pt-BR"));
    }

    private Path logFile() {
        return tempDir.resolve("ftcoin.log");
    }

    private List<String> lines(Path file) throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Sem sessao aberta, nada e gravado em disco")
    void loggingWithoutSessionWritesNoFile() {
        log.walletChanged("create", 1, true);
        log.tradeRegistered("buy", 1, new BigDecimal("10"), DAY);
        log.oracleCacheHit(DAY);
        log.errorCaught(new BusinessException("wallet.msg.invalidData"));

        assertFalse(log.isFileOpen(), "nenhum arquivo deveria estar aberto");
        assertFalse(Files.exists(Paths.get("ftcoin.log")), "nao deveria criar ftcoin.log");
        assertEquals(1, log.count("wallet.create"), "os contadores continuam funcionando");
    }

    @Test
    @DisplayName("A abertura da sessao grava a primeira linha")
    void startSessionWritesTheHeaderLine() throws IOException {
        log.startSession("MEMORY", logFile());

        String first = lines(logFile()).get(0);
        assertTrue(first.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3} INFO .*"),
                "formato inesperado: " + first);
        assertTrue(first.contains("session.start"), first);
        assertTrue(first.contains("persistence=MEMORY"), first);
    }

    @Test
    @DisplayName("Cada linha vai para o disco na hora, sem esperar o buffer")
    void everyLineIsFlushedImmediately() throws IOException {
        // E isto que torna real o acompanhamento com tail -f durante a execucao.
        log.startSession("MEMORY", logFile());
        log.walletChanged("create", 7, true);

        assertTrue(lines(logFile()).stream().anyMatch(line -> line.contains("wallet.create")),
                "a linha deveria estar no arquivo antes de encerrar a sessao");
    }

    @Test
    @DisplayName("O encerramento grava as metricas e fecha o arquivo")
    void endSessionWritesMetricsThenClosesTheFile() throws IOException {
        log.startSession("MEMORY", logFile());
        log.tradeRegistered("buy", 1, new BigDecimal("10"), DAY);
        log.endSession();

        List<String> lines = lines(logFile());
        assertTrue(lines.get(lines.size() - 2).contains("session.metrics"), "penultima linha");
        assertTrue(lines.get(lines.size() - 1).contains("session.end"), "ultima linha");
        assertFalse(log.isFileOpen(), "o arquivo deveria estar fechado");
    }

    @Test
    @DisplayName("As metricas contam acertos, buscas e a taxa de acerto")
    void metricsCountHitsFetchesAndHitRate() {
        log.startSession("MEMORY", logFile());
        log.oracleCacheHit(DAY);
        log.oracleCacheHit(DAY);
        log.oracleFetched(DAY, new BigDecimal("12.50"), 1_000_000L);

        assertEquals(2, log.count("oracle.hit"));
        assertEquals(1, log.count("oracle.fetch"));
        assertTrue(log.metricsLine().contains("hitRate=66.7%"), log.metricsLine());
    }

    @Test
    @DisplayName("O tempo do oraculo e medido em microssegundos")
    void oracleTimingIsMeasuredInMicroseconds() {
        // Um milissegundo sao mil microssegundos: medir em ms daria sempre zero,
        // porque a formula leva poucos microssegundos para ser calculada.
        log.startSession("MEMORY", logFile());
        log.oracleFetched(DAY, new BigDecimal("12.50"), 1_500_000L);

        assertTrue(log.metricsLine().contains("oracleMaxUs=1500"), log.metricsLine());
    }

    @Test
    @DisplayName("Os contadores nao vazam de uma sessao para a seguinte")
    void countersAreClearedBetweenSessions() {
        log.startSession("MEMORY", logFile());
        log.walletChanged("create", 1, true);
        log.endSession();

        assertEquals(0, log.count("wallet.create"));
    }

    @Test
    @DisplayName("Arquivo impossivel de abrir nao derruba a aplicacao")
    void anUnwritableFileDoesNotCrashTheLog() {
        Path impossible = tempDir.resolve("nao/existe/ftcoin.log");

        assertDoesNotThrow(() -> log.startSession("MEMORY", impossible));
        assertFalse(log.isFileOpen(), "nenhum arquivo deveria ter sido aberto");
        assertDoesNotThrow(() -> log.walletChanged("create", 1, true));
        assertEquals(1, log.count("wallet.create"), "os contadores seguem funcionando");
    }

    @Test
    @DisplayName("Encerrar sem ter aberto, ou encerrar duas vezes, nao quebra")
    void endSessionWithoutStartSessionIsHarmless() {
        assertDoesNotThrow(() -> log.endSession());
        assertDoesNotThrow(() -> log.endSession());
    }

    @Test
    @DisplayName("As linhas do registro nao mudam com o idioma da interface")
    void theLogLineIsIndependentOfTheLocale() throws IOException {
        // O arquivo e lido por quem desenvolve e por ferramentas de texto; se o
        // separador decimal seguisse o menu, duas execucoes nao se comparariam.
        MessageProvider.getInstance().setLocale(Locale.forLanguageTag("pt-BR"));
        log.startSession("MEMORY", logFile());
        log.oracleFetched(DAY, new BigDecimal("12.75"), 1_000L);

        String line = lines(logFile()).stream()
                .filter(text -> text.contains("oracle.fetch"))
                .findFirst()
                .orElseThrow();

        assertTrue(line.contains("price=12.75"), "separador decimal deveria ser ponto: " + line);
        assertTrue(line.contains("INFO"), line);
    }
}
