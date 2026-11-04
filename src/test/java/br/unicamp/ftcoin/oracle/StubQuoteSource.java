package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.dto.Quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fonte de cotacoes controlada pelo teste.
 *
 * <p>Permite verificar o algoritmo de apuracao independentemente da formula, e
 * conta quantas vezes foi consultada — o que comprova o cache do
 * {@code OracleService}.</p>
 */
public class StubQuoteSource implements QuoteSource {

    private final Map<LocalDate, BigDecimal> prices = new HashMap<>();
    private int fetchCount;
    private int rangeCount;

    public StubQuoteSource put(LocalDate date, String price) {
        prices.put(date, new BigDecimal(price));
        return this;
    }

    @Override
    public Quote fetch(LocalDate date) {
        fetchCount++;
        BigDecimal price = prices.get(date);
        if (price == null) {
            throw new QuoteUnavailableException("error.oracle.dateBeforeEpoch");
        }
        return new Quote(date, price);
    }

    /** Consulta por intervalo: conta separado e nao passa por fetch. */
    @Override
    public List<Quote> fetchBetween(LocalDate from, LocalDate to) {
        rangeCount++;
        List<Quote> quotes = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            BigDecimal price = prices.get(date);
            if (price != null) {
                quotes.add(new Quote(date, price));
            }
        }
        return quotes;
    }

    public int fetchCount() {
        return fetchCount;
    }

    /** Quantas consultas por intervalo foram feitas. */
    public int rangeCount() {
        return rangeCount;
    }
}
