package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dto.DailyPosition;
import br.unicamp.ftcoin.dto.Quote;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.dto.WalletResult;
import br.unicamp.ftcoin.oracle.FormulaQuoteSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Apuracao de ganhos e perdas por custo medio ponderado.
 *
 * <p>Reproduz a planilha de referencia {@code Docs/FTCoinApuracaoResultado.ods}.
 * Percorrendo os dias com movimentacao em ordem cronologica:</p>
 * <pre>
 *   saldo(n)      = saldo(n-1) + comprado(n) - vendido(n)
 *   custoMedio(n) = (custoMedio(n-1) * saldo(n-1) + comprado(n) * cotacao(n))
 *                   / (saldo(n-1) + comprado(n))        quando houve compra
 *                 = custoMedio(n-1)                     quando nao houve
 *   resultado(n)  = vendido(n) * (cotacao(n) - custoMedio(n))
 * </pre>
 *
 * <p>Tres detalhes definem o resultado e sao faceis de errar: a ponderacao usa o
 * saldo <i>do dia anterior</i>, e nao o total ja comprado; operacoes da mesma
 * data sao somadas em uma unica linha; e numa data com compra e venda a compra
 * entra primeiro, de modo que a venda ja usa o custo medio novo. Os tres vem da
 * planilha.</p>
 *
 * <p>A classe nao conhece DAO nem banco: recebe a lista de movimentacoes pronta.
 * E o que permite testa-la contra a planilha sem infraestrutura.</p>
 */
public final class AverageCostCalculator {

    /**
     * Casas decimais da unica divisao do sistema, a do custo medio.
     *
     * <p>Dez, e nao duas. Com dez casas o custo medio reproduz exatamente o da
     * planilha de referencia (12,1488632292 ao fim da serie de 30 dias); com
     * oito ele ja diverge na nona casa. Dinheiro so e arredondado na exibicao,
     * nunca nos valores intermediarios.</p>
     */
    public static final int COST_SCALE = 10;

    private final OracleService oracleService;

    public AverageCostCalculator(OracleService oracleService) {
        this.oracleService = oracleService;
    }

    /**
     * Apura a carteira inteira.
     *
     * <p>A data de valoracao e um parametro, e nao a data da ultima operacao:
     * o saldo que sobrou continua valendo a cotacao de hoje, mesmo que a ultima
     * compra tenha sido ha meses. E tambem o que torna a apuracao reproduzivel
     * em teste, sem depender do relogio.</p>
     *
     * @param transactions  movimentacoes da carteira, em qualquer ordem
     * @param valuationDate data usada para valorar o saldo final a mercado
     * @return a serie diaria e os totais; zerado quando nao ha movimentacao
     * @throws IllegalStateException se uma venda deixaria o saldo negativo
     */
    public WalletResult calculate(List<Transaction> transactions, LocalDate valuationDate) {
        Quote valuationQuote = oracleService.quoteOn(valuationDate);
        Map<LocalDate, DayTotals> byDate = groupByDate(transactions);
        if (byDate.isEmpty()) {
            return emptyResult(valuationQuote);
        }

        List<DailyPosition> days = new ArrayList<>(byDate.size());
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal averageCost = BigDecimal.ZERO;
        BigDecimal accumulated = BigDecimal.ZERO;
        BigDecimal totalPurchases = BigDecimal.ZERO;
        BigDecimal totalSales = BigDecimal.ZERO;

        for (Map.Entry<LocalDate, DayTotals> entry : byDate.entrySet()) {
            LocalDate date = entry.getKey();
            DayTotals totals = entry.getValue();
            Quote quote = oracleService.quoteOn(date);
            BigDecimal price = quote.price();

            BigDecimal purchaseValue = totals.bought.multiply(price);
            BigDecimal saleValue = totals.sold.multiply(price);

            // A compra do dia entra antes da venda, como na planilha.
            if (totals.bought.signum() > 0) {
                BigDecimal weightedCost = averageCost.multiply(balance).add(purchaseValue);
                balance = balance.add(totals.bought);
                averageCost = weightedCost.divide(balance, COST_SCALE, RoundingMode.HALF_UP);
            }

            BigDecimal operationResult = BigDecimal.ZERO;
            if (totals.sold.signum() > 0) {
                if (totals.sold.compareTo(balance) > 0) {
                    throw new IllegalStateException(
                            "Sale of " + totals.sold + " on " + date + " exceeds the balance of " + balance);
                }
                operationResult = totals.sold.multiply(price.subtract(averageCost));
                balance = balance.subtract(totals.sold);
            }

            accumulated = accumulated.add(operationResult);
            totalPurchases = totalPurchases.add(purchaseValue);
            totalSales = totalSales.add(saleValue);

            days.add(new DailyPosition(
                    FormulaQuoteSource.dayIndexOrZero(date), date,
                    totals.bought, totals.sold, balance,
                    price, averageCost,
                    purchaseValue, saleValue,
                    operationResult, accumulated,
                    averageCost.multiply(balance), price.multiply(balance)));
        }

        return new WalletResult(days, balance, averageCost,
                totalPurchases, totalSales, accumulated,
                averageCost.multiply(balance), valuationQuote.price().multiply(balance),
                valuationQuote);
    }

    /** Soma as operacoes de cada data; o TreeMap garante a ordem cronologica. */
    private Map<LocalDate, DayTotals> groupByDate(List<Transaction> transactions) {
        Map<LocalDate, DayTotals> byDate = new TreeMap<>();
        for (Transaction transaction : transactions) {
            DayTotals totals = byDate.computeIfAbsent(transaction.getDate(), date -> new DayTotals());
            if (transaction.isBuy()) {
                totals.bought = totals.bought.add(transaction.getQuantity());
            } else {
                totals.sold = totals.sold.add(transaction.getQuantity());
            }
        }
        return byDate;
    }

    private WalletResult emptyResult(Quote valuationQuote) {
        return new WalletResult(List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, valuationQuote);
    }

    /** Quantidades compradas e vendidas em uma mesma data. */
    private static final class DayTotals {
        private BigDecimal bought = BigDecimal.ZERO;
        private BigDecimal sold = BigDecimal.ZERO;
    }
}
