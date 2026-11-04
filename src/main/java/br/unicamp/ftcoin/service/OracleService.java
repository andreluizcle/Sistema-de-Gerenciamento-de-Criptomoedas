package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dto.Quote;
import br.unicamp.ftcoin.util.ActivityLog;
import br.unicamp.ftcoin.oracle.QuoteSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Datas que ja foram pedidas a fonte em uma consulta por intervalo.
     *
     * <p>Nem toda data pedida volta com cotacao: o servidor pode simplesmente
     * nao ter a de um dia. Sem registrar o que ja foi perguntado, um intervalo
     * com lacunas nunca contaria como aquecido e cada relatorio repetiria a
     * consulta inteira.</p>
     */
    private final Set<LocalDate> warmedDates = new HashSet<>();
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

    /**
     * Preenche o cache com o intervalo inteiro em uma unica consulta a fonte.
     *
     * <p>E a razao de ser da consulta por faixa: uma apuracao que tocaria trinta
     * datas faz um acesso em vez de trinta. Nao faz nada se o intervalo ja
     * estiver todo em cache e nao engole falhas — uma fonte fora do ar falha
     * aqui com a mesma mensagem com que falharia data a data.</p>
     */
    public void warmUp(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to) || isAlreadyWarmed(from, to)) {
            return;
        }
        long started = System.nanoTime();
        List<Quote> quotes = quoteSource.fetchBetween(from, to);
        log.oracleRangeFetched(from, to, quotes.size(), System.nanoTime() - started);
        for (Quote quote : quotes) {
            cache.put(quote.date(), quote);
        }
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            warmedDates.add(date);
        }
    }

    /**
     * O intervalo ja foi perguntado a fonte, ou ja esta inteiro em cache.
     *
     * <p>Uma data aquecida mas sem cotacao nao e dada como respondida: se alguem
     * pedir essa data especifica, {@link #quoteOn} ainda consulta a fonte e
     * obtem a mensagem de erro precisa.</p>
     */
    private boolean isAlreadyWarmed(LocalDate from, LocalDate to) {
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (!warmedDates.contains(date) && !cache.containsKey(date)) {
                return false;
            }
        }
        return true;
    }

    /** Cotacoes do intervalo, inclusive nas duas pontas. */
    public List<Quote> quotesBetween(LocalDate from, LocalDate to) {
        warmUp(from, to);
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
