package br.unicamp.ftcoin.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Registro de execucao da aplicacao.
 *
 * <p>Grava uma linha por evento em um arquivo de texto e mantem contadores da
 * sessao em memoria. Atende aos dois lados do monitoramento pedido pelo
 * projeto: em tempo real, acompanhando o arquivo com {@code tail -f} enquanto o
 * programa roda; e offline, pelo arquivo que fica em disco depois da execucao e
 * pela linha de metricas gravada no encerramento.</p>
 *
 * <p>E um Singleton pelo mesmo motivo de {@link MessageProvider}: carrega estado
 * com ciclo de vida — o arquivo aberto, os contadores e o instante de inicio — e
 * precisa existir uma unica vez.</p>
 *
 * <p><b>As linhas nunca sao traduzidas.</b> Este arquivo e lido por quem
 * desenvolve, pelo professor e por ferramentas de texto, nao pelo usuario final;
 * um registro cujo idioma mudasse com a escolha do menu nao poderia ser
 * comparado entre execucoes.</p>
 *
 * <p>Escrever no log nunca derruba a aplicacao: nenhum metodo declara excecao e
 * uma falha de E/S apenas desliga o arquivo, mantendo os contadores em memoria.
 * O arquivo so e criado por {@link #startSession(String)}, chamado unicamente
 * pela classe de partida — por isso a suite de testes nao gera arquivo
 * algum.</p>
 */
public final class ActivityLog {

    /** Severidade da linha. */
    public enum Level { INFO, WARN, ERROR }

    private static final String DEFAULT_FILE = "ftcoin.log";
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT);

    private static ActivityLog instance;

    private final Map<String, Integer> counters = new LinkedHashMap<>();

    private BufferedWriter writer;
    private Path file;
    private Instant sessionStart;
    private boolean writeFailed;
    private long oracleMicros;
    private long oracleMaxMicros;
    private long rangeMicros;
    private long reportMicros;

    private ActivityLog() {
    }

    public static synchronized ActivityLog getInstance() {
        if (instance == null) {
            instance = new ActivityLog();
        }
        return instance;
    }

    /** Abre o arquivo indicado por FTCOIN_LOG, ou {@value #DEFAULT_FILE}. */
    public void startSession(String persistence) {
        startSession(persistence, Paths.get(EnvLoader.get("FTCOIN_LOG", DEFAULT_FILE)));
    }

    /**
     * Abre a sessao em um arquivo especifico.
     *
     * <p>O arquivo e aberto em modo de acrescimo: a trilha de auditoria nao e
     * apenas a ultima execucao.</p>
     */
    public synchronized void startSession(String persistence, Path logFile) {
        this.file = logFile;
        this.sessionStart = Instant.now();
        this.writeFailed = false;
        clearMetrics();
        try {
            this.writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            this.writer = null;
            this.writeFailed = true;
            System.err.println("Warning: could not open log file " + logFile + ": " + e.getMessage());
        }
        log(Level.INFO, "session.start", "persistence=" + persistence
                + " locale=" + MessageProvider.getInstance().locale().toLanguageTag());
    }

    /**
     * Grava as metricas, fecha o arquivo e zera o estado.
     *
     * <p>Pode ser chamado sem sessao aberta: nesse caso apenas limpa os
     * contadores. E por isso que vale como reinicio de estado, tanto no
     * {@code finally} da classe de partida quanto entre testes.</p>
     */
    public synchronized void endSession() {
        if (sessionStart != null) {
            log(Level.INFO, "session.metrics", metricsLine());
            log(Level.INFO, "session.end", "file=" + file);
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // Encerrando de qualquer forma; nao ha nada a fazer nem a informar.
            }
        }
        writer = null;
        file = null;
        sessionStart = null;
        clearMetrics();
    }

    private void clearMetrics() {
        counters.clear();
        oracleMicros = 0;
        oracleMaxMicros = 0;
        rangeMicros = 0;
        reportMicros = 0;
    }

    /**
     * Registra um evento. Sem sessao aberta, apenas conta.
     *
     * <p>O dado e gravado imediatamente, sem esperar o buffer encher: e isso que
     * permite acompanhar a execucao em tempo real e nao perder as ultimas linhas
     * se o programa for interrompido.</p>
     */
    public synchronized void log(Level level, String event, String details) {
        counters.merge(event, 1, Integer::sum);
        if (writer == null) {
            return;
        }
        try {
            writer.write(String.format(Locale.ROOT, "%s %-5s %-16s %s",
                    LocalDateTime.now().format(TIMESTAMP), level, event, sanitize(details)));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            writer = null;
            if (!writeFailed) {
                writeFailed = true;
                System.err.println("Warning: could not write to the log file: " + e.getMessage());
            }
        }
    }

    /** Impede que um detalhe com quebra de linha parta o registro em duas. */
    private String sanitize(String details) {
        return details == null ? "" : details.replace('\r', ' ').replace('\n', ' ');
    }

    // ------------------------------------------------------------------
    // Eventos de dominio
    // ------------------------------------------------------------------

    /** Mudanca de estado de uma carteira: create, update ou delete. */
    public void walletChanged(String action, int walletId, boolean applied) {
        log(Level.INFO, "wallet." + action, "id=" + walletId + " applied=" + applied);
    }

    /** Compra ou venda registrada. */
    public void tradeRegistered(String side, int walletId, BigDecimal quantity, LocalDate date) {
        log(Level.INFO, "trade." + side,
                "wallet=" + walletId + " qty=" + quantity.toPlainString() + " date=" + date);
    }

    public void oracleCacheHit(LocalDate date) {
        log(Level.INFO, "oracle.hit", "date=" + date);
    }

    /** Consulta a fonte de cotacao, com o tempo gasto em microssegundos. */
    public synchronized void oracleFetched(LocalDate date, BigDecimal price, long elapsedNanos) {
        long micros = elapsedNanos / 1_000;
        oracleMicros += micros;
        oracleMaxMicros = Math.max(oracleMaxMicros, micros);
        log(Level.INFO, "oracle.fetch",
                "date=" + date + " price=" + price.toPlainString() + " us=" + micros);
    }

    /**
     * Intervalo de cotacoes trazido em uma unica consulta.
     *
     * <p>E a linha que mostra o ganho do pedido por faixa: uma apuracao que
     * pediria dezenas de datas aparece aqui como um unico acesso.</p>
     */
    public synchronized void oracleRangeFetched(LocalDate first, LocalDate last,
                                                int quotes, long elapsedNanos) {
        long micros = elapsedNanos / 1_000;
        rangeMicros += micros;
        log(Level.INFO, "oracle.range",
                "first=" + first + " last=" + last + " quotes=" + quotes + " us=" + micros);
    }

    /** Apuracao de uma carteira, com o tempo gasto em microssegundos. */
    public synchronized void reportBuilt(int walletId, long elapsedNanos) {
        long micros = elapsedNanos / 1_000;
        reportMicros += micros;
        log(Level.INFO, "report.build", "wallet=" + walletId + " us=" + micros);
    }

    /** Estado da conexao com o banco: open, reopen, close ou fail. */
    public void database(String action, String url, Throwable cause) {
        Level level = "fail".equals(action) ? Level.ERROR : Level.INFO;
        log(level, "db." + action, "url=" + url + " cause=" + describe(cause));
    }

    /** Erro tratado e exibido ao usuario. */
    public void errorCaught(FtCoinException e) {
        log(Level.ERROR, "error.caught", "key=" + e.getMessageKey()
                + " type=" + e.getClass().getSimpleName() + " cause=" + describe(e.getCause()));
    }

    public void reportExported(Path exportedFile, int lines) {
        log(Level.INFO, "report.export", "file=" + exportedFile + " lines=" + lines);
    }

    private String describe(Throwable cause) {
        return cause == null ? "none"
                : cause.getClass().getSimpleName() + ":\"" + sanitize(cause.getMessage()) + "\"";
    }

    // ------------------------------------------------------------------
    // Leitura
    // ------------------------------------------------------------------

    public synchronized boolean isFileOpen() {
        return writer != null;
    }

    /** Caminho do arquivo da sessao, ou {@code null} se nao houver. */
    public synchronized Path file() {
        return writer == null ? null : file;
    }

    /** Quantas vezes o evento ocorreu nesta sessao. */
    public synchronized int count(String event) {
        return counters.getOrDefault(event, 0);
    }

    /** Resumo das metricas da sessao, em uma linha. */
    public synchronized String metricsLine() {
        int hits = count("oracle.hit");
        int fetches = count("oracle.fetch");
        int reports = count("report.build");
        long uptime = sessionStart == null ? 0 : Duration.between(sessionStart, Instant.now()).toMillis();
        return String.format(Locale.ROOT,
                "uptimeMs=%d walletCreate=%d walletUpdate=%d walletDelete=%d buy=%d sell=%d"
                        + " oracleHit=%d oracleFetch=%d hitRate=%s oracleAvgUs=%d oracleMaxUs=%d"
                        + " oracleRange=%d oracleRangeAvgUs=%d"
                        + " reportAvgUs=%d dbOpen=%d dbReopen=%d dbFail=%d errors=%d",
                uptime,
                count("wallet.create"), count("wallet.update"), count("wallet.delete"),
                count("trade.buy"), count("trade.sell"),
                hits, fetches, percentage(hits, hits + fetches),
                average(oracleMicros, fetches), oracleMaxMicros,
                count("oracle.range"), average(rangeMicros, count("oracle.range")),
                average(reportMicros, reports),
                count("db.open"), count("db.reopen"), count("db.fail"), count("error.caught"));
    }

    private String percentage(int part, int total) {
        return total == 0 ? "n/a" : String.format(Locale.ROOT, "%.1f%%", 100.0 * part / total);
    }

    private long average(long total, int count) {
        return count == 0 ? 0 : total / count;
    }
}
