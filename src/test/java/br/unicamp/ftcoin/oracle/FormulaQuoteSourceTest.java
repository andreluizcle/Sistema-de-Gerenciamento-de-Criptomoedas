package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.dto.Quote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cotacao calculada pela formula do projeto. */
class FormulaQuoteSourceTest {

    private static final BigDecimal BASE = new BigDecimal("10.00");
    private static final BigDecimal MIN_FLUCTUATION = new BigDecimal("-2.00");
    private static final BigDecimal MAX_FLUCTUATION = new BigDecimal("2.50");

    private final FormulaQuoteSource source = new FormulaQuoteSource();

    /** A parcela de flutuacao, isolada do preco base e da valorizacao. */
    private BigDecimal fluctuationOn(LocalDate date) {
        int dayIndex = FormulaQuoteSource.dayIndexOf(date);
        BigDecimal appreciation = BigDecimal
                .valueOf(Math.log10(25.0 * dayIndex))
                .setScale(FormulaQuoteSource.PRICE_SCALE, RoundingMode.HALF_UP);
        return source.fetch(date).price().subtract(BASE).subtract(appreciation);
    }

    @Test
    @DisplayName("A mesma data devolve sempre a mesma cotacao")
    void isDeterministic() {
        LocalDate date = LocalDate.of(2026, 5, 17);
        BigDecimal first = source.fetch(date).price();
        BigDecimal again = source.fetch(date).price();
        BigDecimal fromAnotherInstance = new FormulaQuoteSource().fetch(date).price();

        assertEquals(first, again, "duas chamadas na mesma instancia");
        assertEquals(first, fromAnotherInstance, "outra instancia");
    }

    @Test
    @DisplayName("Ha cotacao para qualquer data, nao apenas para alguns dias")
    void coversAnyDate() {
        LocalDate date = FormulaQuoteSource.EPOCH;
        for (int day = 0; day < 400; day++, date = date.plusDays(1)) {
            Quote quote = source.fetch(date);
            assertEquals(date, quote.date(), "data da cotacao");
            assertTrue(quote.price().signum() > 0, "cotacao positiva em " + date);
        }
    }

    @Test
    @DisplayName("A cotacao tem dez casas decimais")
    void priceHasTenDecimals() {
        assertEquals(10, source.fetch(LocalDate.of(2026, 3, 9)).price().scale());
    }

    @Test
    @DisplayName("A flutuacao fica entre -2,00 e +2,50, com duas casas")
    void fluctuationStaysInRange() {
        LocalDate date = FormulaQuoteSource.EPOCH;
        for (int day = 0; day < 1000; day++, date = date.plusDays(1)) {
            BigDecimal fluctuation = fluctuationOn(date);
            assertTrue(fluctuation.compareTo(MIN_FLUCTUATION) >= 0,
                    "flutuacao abaixo do minimo em " + date + ": " + fluctuation);
            assertTrue(fluctuation.compareTo(MAX_FLUCTUATION) <= 0,
                    "flutuacao acima do maximo em " + date + ": " + fluctuation);
            assertTrue(fluctuation.stripTrailingZeros().scale() <= 2,
                    "flutuacao com mais de duas casas em " + date + ": " + fluctuation);
        }
    }

    @Test
    @DisplayName("A flutuacao nao repete um degrau constante a cada doze dias")
    void hasNoSeedArtifact() {
        // Semear o sorteio apenas com o numero do dia produzia exatamente
        // +0,08 entre o dia n e o dia n+12, indefinidamente.
        LocalDate start = FormulaQuoteSource.EPOCH;
        Set<BigDecimal> steps = new HashSet<>();
        for (int day = 0; day < 60; day++) {
            BigDecimal step = fluctuationOn(start.plusDays(day + 12L))
                    .subtract(fluctuationOn(start.plusDays(day)));
            steps.add(step);
        }
        assertTrue(steps.size() > 20,
                "a diferenca entre o dia n e o dia n+12 deveria variar, mas assumiu "
                        + steps.size() + " valores distintos");
    }

    @Test
    @DisplayName("A flutuacao cobre bem o intervalo possivel")
    void fluctuationIsWellDistributed() {
        LocalDate date = FormulaQuoteSource.EPOCH;
        Set<BigDecimal> values = new HashSet<>();
        for (int day = 0; day < 2000; day++, date = date.plusDays(1)) {
            values.add(fluctuationOn(date));
        }
        assertTrue(values.size() > 400,
                "apenas " + values.size() + " valores distintos em 2000 dias");
    }

    @Test
    @DisplayName("A formula produz os valores esperados em dias conhecidos")
    void producesKnownPrices() {
        // Valores fixados: se a formula mudar sem querer, estes falham. Nao sao
        // derivados do proprio codigo, ao contrario de uma comparacao com a
        // expressao que se quer verificar, que passaria sempre.
        assertEquals(new BigDecimal("12.1379400087"), priceOnDay(1), "dia 1");
        assertEquals(new BigDecimal("12.0289700043"), priceOnDay(2), "dia 2");
        assertEquals(new BigDecimal("14.2250612634"), priceOnDay(30), "dia 30");
        assertEquals(new BigDecimal("15.6902328731"), priceOnDay(365), "dia 365");
    }

    private BigDecimal priceOnDay(int dayIndex) {
        return source.fetch(FormulaQuoteSource.EPOCH.plusDays(dayIndex - 1L)).price();
    }

    @Test
    @DisplayName("O indice de dia e zero fora da serie, em vez de falhar")
    void dayIndexIsZeroBeforeTheEpoch() {
        // O relatorio exibe este indice como informacao; o servidor de cotacoes
        // cobre datas anteriores a serie da formula e uma movimentacao nessas
        // datas precisa aparecer assim mesmo.
        assertEquals(0, FormulaQuoteSource.dayIndexOrZero(FormulaQuoteSource.EPOCH.minusDays(1)));
        assertEquals(0, FormulaQuoteSource.dayIndexOrZero(FormulaQuoteSource.EPOCH.minusYears(1)));
        assertEquals(1, FormulaQuoteSource.dayIndexOrZero(FormulaQuoteSource.EPOCH));
    }

    @Test
    @DisplayName("Data anterior ao inicio da serie nao tem cotacao")
    void rejectsDateBeforeEpoch() {
        LocalDate before = FormulaQuoteSource.EPOCH.minusDays(1);
        assertThrows(QuoteUnavailableException.class, () -> source.fetch(before));
    }

    @Test
    @DisplayName("O primeiro dia da serie e o indice 1")
    void firstDayIsIndexOne() {
        assertEquals(1, FormulaQuoteSource.dayIndexOf(FormulaQuoteSource.EPOCH));
        assertEquals(30, FormulaQuoteSource.dayIndexOf(FormulaQuoteSource.EPOCH.plusDays(29)));
    }

    @Test
    @DisplayName("Data cujo indice nao cabe em int nao tem cotacao")
    void rejectsDateBeyondTheIndexRange() {
        // A guarda compara no long, antes da conversao. Sem o teto, o indice era
        // truncado nos 32 bits baixos: para algumas datas isso devolvia um
        // numero valido na aparencia, e portanto uma cotacao confiantemente
        // errada, e para outras um indice negativo, que fazia o log10 da
        // valorizacao devolver NaN e o BigDecimal recusar o valor.
        assertThrows(QuoteUnavailableException.class, () -> source.fetch(LocalDate.MAX));
        assertThrows(QuoteUnavailableException.class,
                () -> FormulaQuoteSource.dayIndexOf(LocalDate.MAX));
        assertEquals(0, FormulaQuoteSource.dayIndexOrZero(LocalDate.MAX));
    }

    @Test
    @DisplayName("A busca por intervalo omite as datas sem cotacao, sem falhar")
    void fetchBetweenSkipsDatesWithoutQuote() {
        // Exercita a implementacao padrao de QuoteSource.fetchBetween, que a
        // formula nao sobrescreve. O contrato e de melhor esforco: propagar a
        // falha fazia uma carteira com movimentacao anterior a 2026 perder o
        // relatorio inteiro no aquecimento do cache, antes de o calculo comecar.
        LocalDate from = FormulaQuoteSource.EPOCH.minusDays(2);
        LocalDate to = FormulaQuoteSource.EPOCH.plusDays(1);

        var quotes = source.fetchBetween(from, to);

        assertEquals(2, quotes.size(), "so os dois dias dentro da serie");
        assertEquals(FormulaQuoteSource.EPOCH, quotes.get(0).date());
        assertEquals(FormulaQuoteSource.EPOCH.plusDays(1), quotes.get(1).date());
    }
}
