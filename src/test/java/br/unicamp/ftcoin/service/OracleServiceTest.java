package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dto.Quote;
import br.unicamp.ftcoin.oracle.QuoteSource;
import br.unicamp.ftcoin.oracle.QuoteUnavailableException;
import br.unicamp.ftcoin.oracle.StubQuoteSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Cache de cotacoes: a evidencia de que a rede e consultada o minimo possivel. */
class OracleServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 1);

    @Test
    @DisplayName("A mesma data e buscada na fonte uma unica vez")
    void fetchesEachDateOnlyOnce() {
        StubQuoteSource source = new StubQuoteSource().put(DAY, "12.50");
        OracleService service = new OracleService(source);

        service.quoteOn(DAY);
        service.quoteOn(DAY);
        service.quoteOn(DAY);

        assertEquals(1, source.fetchCount(), "consultas a fonte");
        assertEquals(1, service.cachedQuoteCount(), "datas em cache");
    }

    @Test
    @DisplayName("Um intervalo inteiro e trazido em uma unica consulta")
    void fetchesAWholeRangeInOneCall() {
        StubQuoteSource source = new StubQuoteSource()
                .put(DAY, "12.50")
                .put(DAY.plusDays(1), "13.00")
                .put(DAY.plusDays(2), "11.80");
        OracleService service = new OracleService(source);

        service.quotesBetween(DAY, DAY.plusDays(2));
        service.quotesBetween(DAY, DAY.plusDays(2));

        assertEquals(1, source.rangeCount(), "uma consulta por intervalo, nao uma por data");
        assertEquals(0, source.fetchCount(), "nenhuma consulta data a data");
        assertEquals(3, service.cachedQuoteCount(), "datas em cache");
    }

    @Test
    @DisplayName("Aquecer um intervalo ja em cache nao consulta a fonte")
    void warmUpIsIdempotent() {
        StubQuoteSource source = new StubQuoteSource()
                .put(DAY, "12.50")
                .put(DAY.plusDays(1), "13.00");
        OracleService service = new OracleService(source);

        service.warmUp(DAY, DAY.plusDays(1));
        service.warmUp(DAY, DAY.plusDays(1));

        assertEquals(1, source.rangeCount(), "a segunda chamada nao deveria consultar");
    }

    @Test
    @DisplayName("Intervalo invertido ou nulo nao consulta a fonte")
    void warmUpIgnoresInvalidRange() {
        StubQuoteSource source = new StubQuoteSource().put(DAY, "12.50");
        OracleService service = new OracleService(source);

        service.warmUp(DAY.plusDays(5), DAY);
        service.warmUp(null, DAY);

        assertEquals(0, source.rangeCount(), "nao deveria ter consultado");
    }

    @Test
    @DisplayName("O intervalo devolve uma cotacao por dia, nas duas pontas inclusive")
    void quotesBetweenIsInclusive() {
        StubQuoteSource source = new StubQuoteSource()
                .put(DAY, "12.50")
                .put(DAY.plusDays(1), "13.00");
        OracleService service = new OracleService(source);

        assertEquals(2, service.quotesBetween(DAY, DAY.plusDays(1)).size());
    }

    @Test
    @DisplayName("Cotacao com data fora do intervalo pedido nao entra no cache")
    void warmUpIgnoresQuotesOutsideTheRange() {
        // Uma resposta com data fora da faixa — erro de fuso ou de intervalo no
        // servidor — passava a valer como a cotacao daquele dia pelo resto da
        // sessao, e uma operacao podia ser registrada a um preco que o servidor
        // nunca publicou para ela.
        LocalDate intrusa = DAY.plusYears(2);
        OracleService service = new OracleService(new FixedRangeSource(List.of(
                new Quote(DAY, new BigDecimal("12.50")),
                new Quote(intrusa, new BigDecimal("99.99")))));

        service.warmUp(DAY, DAY.plusDays(1));

        assertEquals(1, service.cachedQuoteCount(), "so a data pedida deveria estar em cache");
        assertEquals(new BigDecimal("12.50"), service.quoteOn(DAY).price());
    }

    @Test
    @DisplayName("O aquecimento nao sobrescreve cotacao ja em cache")
    void warmUpDoesNotOverwriteACachedQuote() {
        // A primeira resposta vence, como em ApiQuoteSource.fetch. Sem isso,
        // duas consultas da mesma sessao podiam devolver precos diferentes para
        // a mesma data.
        OracleService service = new OracleService(new FixedRangeSource(
                List.of(new Quote(DAY, new BigDecimal("12.50"))),
                List.of(new Quote(DAY, new BigDecimal("77.77")))));

        assertEquals(new BigDecimal("12.50"), service.quoteOn(DAY).price(), "consulta avulsa");
        service.warmUp(DAY, DAY.plusDays(1));
        assertEquals(new BigDecimal("12.50"), service.quoteOn(DAY).price(), "depois do aquecimento");
    }

    @Test
    @DisplayName("Intervalo absurdamente largo nao e buscado em bloco")
    void warmUpSkipsAnAbsurdlyWideRange() {
        // A implementacao padrao de fetchBetween materializa uma cotacao por
        // dia. Com uma data em ano distante na base, o relatorio tentava montar
        // milhoes de objetos e terminava sem memoria.
        StubQuoteSource source = new StubQuoteSource().put(DAY, "12.50");
        OracleService service = new OracleService(source);

        service.warmUp(DAY, DAY.plusYears(500));

        assertEquals(0, source.rangeCount(), "nao deveria ter buscado o intervalo");
        assertEquals(0, service.cachedQuoteCount(), "nada deveria ter entrado em cache");
    }

    /**
     * Fonte que devolve uma lista fixa na consulta por intervalo.
     *
     * <p>Diferente de {@code StubQuoteSource}, pode devolver data fora do
     * intervalo pedido e preco diferente do da consulta avulsa — e o que permite
     * verificar o filtro e a regra de nao sobrescrever.</p>
     */
    private static final class FixedRangeSource implements QuoteSource {

        private final List<Quote> single;
        private final List<Quote> range;

        FixedRangeSource(List<Quote> range) {
            this(range, range);
        }

        FixedRangeSource(List<Quote> single, List<Quote> range) {
            this.single = single;
            this.range = range;
        }

        @Override
        public Quote fetch(LocalDate date) {
            return single.stream()
                    .filter(quote -> quote.date().equals(date))
                    .findFirst()
                    .orElseThrow(() -> new QuoteUnavailableException("error.oracle.noQuote"));
        }

        @Override
        public List<Quote> fetchBetween(LocalDate from, LocalDate to) {
            return range;
        }
    }
}
