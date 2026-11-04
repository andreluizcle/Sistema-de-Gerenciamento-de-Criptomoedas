package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dto.Quote;
import br.unicamp.ftcoin.util.ActivityLog;
import br.unicamp.ftcoin.oracle.QuoteSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consulta de cotacoes com cache.
 *
 * <p>O cache atende o requisito de reduzir operacoes de rede: cada data e
 * buscada na fonte uma unica vez por execucao, e as consultas seguintes saem da
 * memoria. Isso importa principalmente na apuracao, que percorre o historico
 * inteiro da carteira e repete datas com frequencia.</p>
 *
 * <p>O cache vive enquanto o programa roda e nao tem limite de tamanho, o que e
 * adequado para uma sessao de linha de comando.</p>
 */
public class OracleService {

    private final QuoteSource quoteSource;
    private final Map<LocalDate, Quote> cache = new HashMap<>();
    private final ActivityLog log = ActivityLog.getInstance();

    public OracleService(QuoteSource quoteSource) {
        this.quoteSource = quoteSource;
    }

    /** Cotacao da data, buscando na fonte apenas na primeira vez. */
    public Quote quoteOn(LocalDate date) {
        Quote cached = cache.get(date);
        if (cached != null) {
            log.oracleCacheHit(date);
            return cached;
        }
        // Nada e gravado no cache se a busca falhar, como acontecia com computeIfAbsent.
        long started = System.nanoTime();
        Quote fetched = quoteSource.fetch(date);
        log.oracleFetched(date, fetched.price(), System.nanoTime() - started);
        cache.put(date, fetched);
        return fetched;
    }

    public Quote todayQuote() {
        return quoteOn(LocalDate.now());
    }

    public BigDecimal priceOn(LocalDate date) {
        return quoteOn(date).price();
    }

    /** Cotacoes do intervalo, inclusive nas duas pontas. */
    public List<Quote> quotesBetween(LocalDate from, LocalDate to) {
        List<Quote> quotes = new ArrayList<>();
        for (long offset = 0; offset <= ChronoUnit.DAYS.between(from, to); offset++) {
            quotes.add(quoteOn(from.plusDays(offset)));
        }
        return quotes;
    }

    /** Quantas datas distintas ja estao em cache. */
    public int cachedQuoteCount() {
        return cache.size();
    }
}
