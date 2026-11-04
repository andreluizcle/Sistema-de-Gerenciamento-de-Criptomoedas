package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dto.Quote;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.dto.Wallet;
import br.unicamp.ftcoin.dto.WalletResult;
import br.unicamp.ftcoin.util.ActivityLog;
import br.unicamp.ftcoin.dto.WalletSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reune os numeros ja calculados que os relatorios exibem.
 *
 * <p>Existe para que telas e controladores recebam um objeto pronto em vez de
 * varios valores soltos do mesmo tipo, que era de onde vinham as listas longas
 * de parametros.</p>
 */
public class ReportService {

    private final TransactionService transactionService;
    private final OracleService oracleService;
    private final AverageCostCalculator calculator;

    public ReportService(TransactionService transactionService, OracleService oracleService) {
        this.transactionService = transactionService;
        this.oracleService = oracleService;
        this.calculator = new AverageCostCalculator(oracleService);
    }

    /**
     * Apuracao completa da carteira (relatorios 4 e 5).
     *
     * <p>O saldo remanescente e valorado pela cotacao de hoje, que e o criterio
     * da planilha de referencia: ela valora a mercado no ultimo dia da serie, e
     * nao no dia da ultima operacao.</p>
     */
    public WalletResult resultOf(int walletId) {
        long started = System.nanoTime();
        LocalDate valuationDate = LocalDate.now();
        List<Transaction> history = transactionService.historyOf(walletId);
        warmUpFor(history, valuationDate);
        WalletResult result = calculator.calculate(history, valuationDate);
        ActivityLog.getInstance().reportBuilt(walletId, System.nanoTime() - started);
        return result;
    }

    /**
     * Traz de uma vez as cotacoes que a apuracao vai pedir.
     *
     * <p>Sem isto, com o oraculo em rede, a apuracao faria um acesso por data do
     * historico. O intervalo cobre da primeira a ultima movimentacao; a data de
     * valoracao e buscada a parte, para nao pedir ao servidor os dias ociosos
     * entre a ultima operacao e hoje.</p>
     */
    private void warmUpFor(List<Transaction> history, LocalDate valuationDate) {
        if (history.isEmpty()) {
            return;
        }
        oracleService.warmUp(history.get(0).getDate(), history.get(history.size() - 1).getDate());
    }

    /** Relatorio 4: movimentacoes da carteira em ordem cronologica. */
    public List<Transaction> historyOf(int walletId) {
        return transactionService.historyOf(walletId);
    }

    /**
     * Saldo valorado da carteira (relatorio 3).
     *
     * <p>Diferente de {@link #resultOf}, usa a cotacao de <b>hoje</b>: a pergunta
     * aqui e quanto a carteira vale agora, nao como ela fechou.</p>
     */
    public WalletSummary summaryOf(Wallet wallet) {
        WalletResult result = resultOf(wallet.getId());
        Quote today = oracleService.todayQuote();
        BigDecimal balance = result.balance();
        return new WalletSummary(
                wallet,
                balance,
                result.averageCost(),
                result.averageCost().multiply(balance),
                today.price().multiply(balance),
                today);
    }
}
