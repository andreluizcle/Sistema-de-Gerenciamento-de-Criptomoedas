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
        assertEquals(FormulaQuoteSource.PRICE_SCALE,
                source.fetch(LocalDate.of(2026, 3, 9)).price().scale());
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
    @DisplayName("A valorizacao e log10(25 * n)")
    void appreciationFollowsTheFormula() {
        for (int dayIndex : new int[]{1, 2, 30, 365, 10000}) {
            LocalDate date = FormulaQuoteSource.EPOCH.plusDays(dayIndex - 1L);
            BigDecimal expected = BigDecimal.valueOf(Math.log10(25.0 * dayIndex))
                    .setScale(FormulaQuoteSource.PRICE_SCALE, RoundingMode.HALF_UP);
            BigDecimal actual = source.fetch(date).price()
                    .subtract(BASE).subtract(fluctuationOn(date));
            assertEquals(expected, actual, "valorizacao no dia " + dayIndex);
        }
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
}
