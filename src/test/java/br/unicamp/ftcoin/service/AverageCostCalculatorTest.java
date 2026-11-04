package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dto.DailyPosition;
import br.unicamp.ftcoin.dto.OperationType;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.dto.WalletResult;
import br.unicamp.ftcoin.oracle.StubQuoteSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Apuracao por custo medio ponderado. */
class AverageCostCalculatorTest {

    private AverageCostCalculator calculatorFor(StubQuoteSource source) {
        return new AverageCostCalculator(new OracleService(source));
    }

    /**
     * Compara o custo medio em oito casas.
     *
     * <p>A partir da nona casa a planilha e o sistema divergem por construcao: ela
     * calcula com a cotacao em precisao total de ponto flutuante e o sistema
     * trabalha com as dez casas que armazena. Ao fim dos 30 dias os dois voltam a
     * coincidir, e por isso o teste da serie completa compara o valor exato.</p>
     */
    private void assertAverageCost(String expected, BigDecimal actual, String label) {
        assertEquals(new BigDecimal(expected).setScale(8, RoundingMode.HALF_UP),
                actual.setScale(8, RoundingMode.HALF_UP), label);
    }

    /** Compara dinheiro em centavos, que e como o valor chega ao usuario. */
    private void assertMoney(String expected, BigDecimal actual, String label) {
        assertEquals(new BigDecimal(expected),
                actual.setScale(2, RoundingMode.HALF_UP), label);
    }

    @Test
    @DisplayName("Reproduz a planilha de referencia nos 30 dias e nos cinco totais")
    void reproducesReferenceSpreadsheet() {
        WalletResult result = calculatorFor(ReferenceSpreadsheet.quoteSource())
                .calculate(ReferenceSpreadsheet.transactionsUntil(30),
                        ReferenceSpreadsheet.day(30));

        assertEquals(0, result.balance().compareTo(new BigDecimal("1000")), "saldo");
        assertEquals(new BigDecimal("12.1488632292"), result.averageCost(), "custo medio");
        assertMoney("76798.50", result.totalPurchases(), "total comprado");
        assertMoney("61507.89", result.totalSales(), "total vendido");
        assertMoney("-3141.75", result.accumulatedResult(), "resultado acumulado");
        assertMoney("12148.86", result.costBalance(), "saldo ao custo medio");
        assertMoney("14655.06", result.marketBalance(), "saldo a mercado");
    }

    @Test
    @DisplayName("Reproduz a planilha tambem num corte de dez dias")
    void reproducesFirstTenDays() {
        WalletResult result = calculatorFor(ReferenceSpreadsheet.quoteSource())
                .calculate(ReferenceSpreadsheet.transactionsUntil(10),
                        ReferenceSpreadsheet.day(10));

        assertEquals(0, result.balance().compareTo(new BigDecimal("500")), "saldo");
        assertAverageCost("13.1667075089", result.averageCost(), "custo medio");
        assertMoney("-897.16", result.accumulatedResult(), "resultado acumulado");
        assertMoney("6583.35", result.costBalance(), "saldo ao custo medio");
        assertMoney("6918.97", result.marketBalance(), "saldo a mercado");
    }

    @Test
    @DisplayName("Carteira sem movimentacao devolve tudo zerado, sem erro")
    void emptyWallet() {
        WalletResult result = calculatorFor(ReferenceSpreadsheet.quoteSource())
                .calculate(List.of(), ReferenceSpreadsheet.day(1));

        assertTrue(result.days().isEmpty(), "serie diaria");
        assertEquals(0, result.balance().signum(), "saldo");
        assertEquals(0, result.averageCost().signum(), "custo medio");
        assertEquals(0, result.accumulatedResult().signum(), "resultado");
    }

    @Test
    @DisplayName("Uma unica compra fixa o custo medio na cotacao do dia")
    void singlePurchaseSetsAverageCostToThePrice() {
        WalletResult result = calculatorFor(ReferenceSpreadsheet.quoteSource())
                .calculate(ReferenceSpreadsheet.transactionsUntil(1),
                        ReferenceSpreadsheet.day(1));

        assertEquals(new BigDecimal("12.2879400087"), result.averageCost(), "custo medio");
        assertMoney("12287.94", result.costBalance(), "saldo ao custo medio");
        assertEquals(0, result.accumulatedResult().signum(), "ainda nao ha resultado realizado");
    }

    @Test
    @DisplayName("Venda nao altera o custo medio")
    void saleKeepsTheAverageCost() {
        WalletResult result = calculatorFor(ReferenceSpreadsheet.quoteSource())
                .calculate(ReferenceSpreadsheet.transactionsUntil(5),
                        ReferenceSpreadsheet.day(5));

        assertEquals(new BigDecimal("12.2879400087"), result.averageCost(),
                "o custo medio da compra do dia 1 permanece apos a venda do dia 5");
        assertMoney("-865.51", result.accumulatedResult(), "resultado realizado");
    }

    @Test
    @DisplayName("A ponderacao usa o saldo anterior, nao o total ja comprado")
    void weightsByPreviousBalance() {
        // Dia 6: compra 1.000 sobre um saldo de 500. Ponderando pelo saldo
        // anterior o custo medio da 13,1667; pelo total comprado daria 12,95.
        WalletResult result = calculatorFor(ReferenceSpreadsheet.quoteSource())
                .calculate(ReferenceSpreadsheet.transactionsUntil(6),
                        ReferenceSpreadsheet.day(6));

        assertAverageCost("13.1667075089", result.averageCost(), "custo medio");
        assertMoney("19750.06", result.costBalance(), "saldo ao custo medio");
    }

    @Test
    @DisplayName("Compras na mesma data viram uma linha so")
    void sameDatePurchasesAreAggregated() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        StubQuoteSource source = new StubQuoteSource().put(date, "10.00");
        List<Transaction> transactions = List.of(
                new Transaction(1, 1, date, OperationType.BUY, new BigDecimal("100")),
                new Transaction(2, 1, date, OperationType.BUY, new BigDecimal("300")));

        WalletResult result = calculatorFor(source).calculate(transactions, date);

        assertEquals(1, result.days().size(), "uma linha por data");
        assertEquals(0, result.days().get(0).bought().compareTo(new BigDecimal("400")), "comprado");
        assertEquals(0, result.balance().compareTo(new BigDecimal("400")), "saldo");
    }

    @Test
    @DisplayName("Na mesma data a compra entra antes da venda")
    void purchaseIsAppliedBeforeSaleOnTheSameDate() {
        LocalDate first = LocalDate.of(2026, 1, 1);
        LocalDate second = LocalDate.of(2026, 1, 2);
        StubQuoteSource source = new StubQuoteSource()
                .put(first, "10.00").put(second, "20.00");
        List<Transaction> transactions = List.of(
                new Transaction(1, 1, first, OperationType.BUY, new BigDecimal("100")),
                new Transaction(2, 1, second, OperationType.BUY, new BigDecimal("100")),
                new Transaction(3, 1, second, OperationType.SELL, new BigDecimal("100")));

        WalletResult result = calculatorFor(source).calculate(transactions, second);

        // Custo medio apos a compra do dia 2: (10*100 + 100*20) / 200 = 15.
        // A venda usa esse custo novo: 100 * (20 - 15) = 500.
        assertEquals(new BigDecimal("15.0000000000"), result.averageCost(), "custo medio");
        assertMoney("500.00", result.accumulatedResult(), "resultado realizado");
    }

    @Test
    @DisplayName("A ordem em que as movimentacoes chegam nao altera o resultado")
    void sortsTransactionsItself() {
        List<Transaction> ordered = ReferenceSpreadsheet.transactionsUntil(10);
        List<Transaction> shuffled = new ArrayList<>(ordered);
        Collections.reverse(shuffled);

        WalletResult fromOrdered = calculatorFor(ReferenceSpreadsheet.quoteSource())
                .calculate(ordered, ReferenceSpreadsheet.day(10));
        WalletResult fromShuffled = calculatorFor(ReferenceSpreadsheet.quoteSource())
                .calculate(shuffled, ReferenceSpreadsheet.day(10));

        assertEquals(fromOrdered.averageCost(), fromShuffled.averageCost(), "custo medio");
        assertEquals(fromOrdered.accumulatedResult(), fromShuffled.accumulatedResult(), "resultado");
    }

    @Test
    @DisplayName("Vender tudo zera o saldo sem divisao por zero")
    void sellingTheWholeBalance() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        StubQuoteSource source = new StubQuoteSource().put(date, "10.00");
        List<Transaction> transactions = List.of(
                new Transaction(1, 1, date, OperationType.BUY, new BigDecimal("100")),
                new Transaction(2, 1, date, OperationType.SELL, new BigDecimal("100")));

        WalletResult result = calculatorFor(source).calculate(transactions, date);

        assertEquals(0, result.balance().signum(), "saldo");
        assertEquals(0, result.costBalance().signum(), "saldo ao custo medio");
        assertEquals(0, result.marketBalance().signum(), "saldo a mercado");
    }

    @Test
    @DisplayName("Venda maior que o saldo e recusada pelo calculo")
    void rejectsSaleBeyondTheBalance() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        StubQuoteSource source = new StubQuoteSource().put(date, "10.00");
        List<Transaction> transactions = List.of(
                new Transaction(1, 1, date, OperationType.SELL, new BigDecimal("100")));

        AverageCostCalculator calculator = calculatorFor(source);
        assertThrows(IllegalStateException.class,
                () -> calculator.calculate(transactions, date));
    }

    @Test
    @DisplayName("Ha uma linha por data com movimentacao")
    void oneRowPerDateWithMovement() {
        WalletResult result = calculatorFor(ReferenceSpreadsheet.quoteSource())
                .calculate(ReferenceSpreadsheet.transactionsUntil(30),
                        ReferenceSpreadsheet.day(30));

        // 6 compras e 10 vendas, sem coincidencia de datas entre elas.
        assertEquals(16, result.days().size(), "dias com movimentacao");
        DailyPosition firstDay = result.days().get(0);
        assertEquals(1, firstDay.dayIndex(), "indice do primeiro dia");
        assertEquals(ReferenceSpreadsheet.day(1), firstDay.date(), "data do primeiro dia");
    }
}
