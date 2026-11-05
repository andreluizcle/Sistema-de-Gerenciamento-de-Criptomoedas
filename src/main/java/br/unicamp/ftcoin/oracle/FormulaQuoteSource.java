package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.dto.Quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * Cotacao calculada pela formula do projeto, para qualquer data.
 *
 * <p>Reproduz a simulacao de {@code Docs/FTCoinApuracaoResultado.ods}:</p>
 * <pre>
 *   cotacao(n) = 10,00 + flutuacao(n) + log10(25 * n)
 * </pre>
 * <p>onde {@code n} e o numero do dia na serie, contado a partir de
 * {@value #EPOCH_DESCRIPTION}. O termo de valorizacao faz o preco subir devagar
 * ao longo do tempo e a flutuacao responde pela variacao diaria.</p>
 *
 * <p>Na planilha a flutuacao vem de {@code RANDBETWEEN(-200;+250)/100}, que
 * sorteia um valor novo a cada recalculo. Aqui ela precisa ser estavel: o mesmo
 * dia tem de devolver sempre a mesma cotacao, senao dois relatorios seguidos
 * divergem e nenhum teste e reproduzivel. Por isso o sorteio e semeado pelo
 * proprio dia — o algoritmo de {@link Random} e especificado pela JDK, entao o
 * resultado e identico em qualquer maquina — e a cotacao vira uma funcao pura da
 * data, sem estado e sem armazenamento.</p>
 */
public final class FormulaQuoteSource implements QuoteSource {

    /** Primeiro dia da serie, como na planilha de referencia. */
    public static final LocalDate EPOCH = LocalDate.of(2026, 1, 1);
    static final String EPOCH_DESCRIPTION = "2026-01-01";

    private static final BigDecimal BASE_PRICE = new BigDecimal("10.00");
    private static final int LOG_FACTOR = 25;
    private static final int MIN_FLUCTUATION_CENTS = -200;
    private static final int MAX_FLUCTUATION_CENTS = 250;
    private static final int FLUCTUATION_RANGE = MAX_FLUCTUATION_CENTS - MIN_FLUCTUATION_CENTS + 1;

    /**
     * Semear o sorteio apenas com o numero do dia nao basta: o embaralhador de
     * {@link Random} avanca pouco entre sementes consecutivas, e os dias n e
     * n+12 acabariam diferindo por exatos 0,08 — um padrao visivel num grafico.
     * Multiplicar por uma constante impar de 32 bits espalha as sementes.
     */
    private static final long SEED_MIXER = 2_654_435_761L;

    /**
     * Casas decimais da cotacao. Dez, e nao duas: a planilha mostra dois digitos
     * mas calcula com a precisao cheia, e arredondar aqui desloca o resultado
     * acumulado em centavos ao longo de dezenas de operacoes.
     */
    public static final int PRICE_SCALE = 10;

    @Override
    public Quote fetch(LocalDate date) {
        int dayIndex = dayIndexOf(date);
        BigDecimal price = BASE_PRICE
                .add(fluctuationOn(dayIndex))
                .add(appreciationOn(dayIndex))
                .setScale(PRICE_SCALE, RoundingMode.HALF_UP);
        return new Quote(date, price);
    }

    /** Numero do dia na serie, comecando em 1 na data de {@link #EPOCH}. */
    public static int dayIndexOf(LocalDate date) {
        long index = ChronoUnit.DAYS.between(EPOCH, date) + 1;
        if (index < 1) {
            throw new QuoteUnavailableException("error.oracle.dateBeforeEpoch");
        }
        return (int) index;
    }

    /**
     * Numero do dia na serie, ou zero se a data for anterior ao inicio.
     *
     * <p>Serve a quem exibe o indice como informacao e nao pode falhar por
     * isso. O servidor de cotacoes cobre datas fora da serie da formula, e uma
     * movimentacao nessas datas deve aparecer no relatorio mesmo assim.</p>
     */
    public static int dayIndexOrZero(LocalDate date) {
        long index = ChronoUnit.DAYS.between(EPOCH, date) + 1;
        return index < 1 ? 0 : (int) index;
    }

    /** Termo log10(25 * n) da planilha. */
    private BigDecimal appreciationOn(int dayIndex) {
        double appreciation = Math.log10((double) LOG_FACTOR * dayIndex);
        return BigDecimal.valueOf(appreciation).setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /** Equivalente deterministico de RANDBETWEEN(-200;+250)/100. */
    private BigDecimal fluctuationOn(int dayIndex) {
        Random random = new Random(dayIndex * SEED_MIXER);
        int cents = MIN_FLUCTUATION_CENTS + random.nextInt(FLUCTUATION_RANGE);
        return BigDecimal.valueOf(cents, 2);
    }
}
