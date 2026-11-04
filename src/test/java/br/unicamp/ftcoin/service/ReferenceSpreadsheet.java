package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dto.OperationType;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.oracle.StubQuoteSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A simulacao de {@code Docs/FTCoinApuracaoResultado.ods}, transcrita.
 *
 * <p>Sao os 30 dias de 01/01/2026 a 30/01/2026 com as cotacoes que a planilha
 * tem gravadas, truncadas para as dez casas usadas pelo sistema, mais as compras
 * de 1.000 FTC e as vendas de 500 FTC nos mesmos dias. Serve para conferir a
 * apuracao contra um resultado produzido fora do projeto.</p>
 */
final class ReferenceSpreadsheet {

    static final LocalDate FIRST_DAY = LocalDate.of(2026, 1, 1);
    static final int DAYS = 30;

    /** Cotacoes dos dias 1 a 30, como na coluna "Cotacao" da planilha. */
    static final String[] PRICES = {
            "12.2879400087", "13.8689700043", "12.1650612634", "12.1000000000", "10.5569100130",
            "13.6060912591", "14.0130380487", "11.1210299957", "12.4321825181", "13.8379400087",
            "14.2093326938", "13.4371212547", "11.4718833610", "14.2240680444", "12.5640312677",
            "12.6020599913", "12.9583889301", "14.6132125138", "14.6066936096", "11.5589700043",
            "12.0601593034", "14.1603626895", "10.9196678447", "11.7981512504", "13.0258800173",
            "12.0329133566", "10.8493037728", "12.0350980400", "11.8303380066", "14.6550612634"
    };

    /** Dias com compra de 1.000 FTC. */
    static final int[] BUY_DAYS = {1, 6, 11, 16, 21, 26};

    /** Dias com venda de 500 FTC. */
    static final int[] SELL_DAYS = {5, 9, 10, 13, 15, 18, 20, 23, 25, 28};

    private ReferenceSpreadsheet() {
    }

    static LocalDate day(int dayNumber) {
        return FIRST_DAY.plusDays(dayNumber - 1L);
    }

    static StubQuoteSource quoteSource() {
        StubQuoteSource source = new StubQuoteSource();
        for (int dayNumber = 1; dayNumber <= DAYS; dayNumber++) {
            source.put(day(dayNumber), PRICES[dayNumber - 1]);
        }
        return source;
    }

    /** As movimentacoes da planilha, ate o dia informado inclusive. */
    static List<Transaction> transactionsUntil(int lastDay) {
        List<Transaction> transactions = new ArrayList<>();
        int id = 0;
        for (int dayNumber = 1; dayNumber <= lastDay; dayNumber++) {
            if (contains(BUY_DAYS, dayNumber)) {
                transactions.add(new Transaction(++id, 1, day(dayNumber),
                        OperationType.BUY, new BigDecimal("1000.000")));
            }
            if (contains(SELL_DAYS, dayNumber)) {
                transactions.add(new Transaction(++id, 1, day(dayNumber),
                        OperationType.SELL, new BigDecimal("500.000")));
            }
        }
        return transactions;
    }

    private static boolean contains(int[] days, int dayNumber) {
        for (int day : days) {
            if (day == dayNumber) {
                return true;
            }
        }
        return false;
    }
}
