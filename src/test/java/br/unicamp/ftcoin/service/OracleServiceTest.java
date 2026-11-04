package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.oracle.StubQuoteSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
    @DisplayName("Datas distintas sao buscadas uma vez cada")
    void fetchesEachDistinctDateOnce() {
        StubQuoteSource source = new StubQuoteSource()
                .put(DAY, "12.50")
                .put(DAY.plusDays(1), "13.00")
                .put(DAY.plusDays(2), "11.80");
        OracleService service = new OracleService(source);

        service.quotesBetween(DAY, DAY.plusDays(2));
        service.quotesBetween(DAY, DAY.plusDays(2));

        assertEquals(3, source.fetchCount(), "consultas a fonte");
        assertEquals(3, service.cachedQuoteCount(), "datas em cache");
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
}
