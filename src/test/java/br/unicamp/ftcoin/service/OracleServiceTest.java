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
}
