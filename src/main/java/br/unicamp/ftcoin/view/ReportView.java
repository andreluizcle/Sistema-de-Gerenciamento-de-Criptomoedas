package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.dto.DailyPosition;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.dto.Wallet;
import br.unicamp.ftcoin.dto.WalletResult;
import br.unicamp.ftcoin.dto.WalletSummary;
import br.unicamp.ftcoin.util.ActivityLog;
import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.ReportPrinter;

import javax.print.PrintException;
import javax.print.PrintService;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tela dos cinco relatorios.
 *
 * <p>Cada relatorio e montado como uma lista de linhas, exibida na tela e, se o
 * usuario quiser, enviada para impressao. Assim o que sai no papel e exatamente
 * o que apareceu na tela, sem uma segunda rotina de formatacao.</p>
 */
public class ReportView extends BaseView {

    private static final String RULE = "-".repeat(72);

    public int showMenu() {
        header("menu.report.title");
        for (int option = 1; option <= 5; option++) {
            System.out.println(messages.get("menu.report." + option));
        }
        System.out.println(messages.get("menu.report.0"));
        return readInt("menu.main.choose");
    }

    public int readWalletId() {
        return readInt("transaction.input.walletId");
    }

    /** Relatorios 1 e 2: carteiras ordenadas por identificador ou por titular. */
    public void showWalletList(String titleKey, List<Wallet> wallets) {
        List<String> lines = new ArrayList<>();
        lines.add(String.format(messages.locale(), "%-6s %-30s %-20s",
                messages.get("report.header.id"),
                messages.get("report.header.holder"),
                messages.get("report.header.brokerage")));
        lines.add(RULE);
        for (Wallet wallet : wallets) {
            lines.add(String.format(messages.locale(), "%-6d %-30s %-20s",
                    wallet.getId(), wallet.getHolderName(), wallet.getBrokerage()));
        }
        if (wallets.isEmpty()) {
            lines.add(messages.get("report.msg.noWallets"));
        }
        render(messages.get(titleKey), lines);
    }

    /** Relatorio 3: saldo atual de uma carteira. */
    public void showSummary(WalletSummary summary) {
        List<String> lines = List.of(
                messages.format("report.msg.walletLine",
                        summary.wallet().getId(), summary.wallet().getHolderName()),
                RULE,
                messages.format("report.msg.balance", messages.quantity(summary.balance())),
                messages.format("report.msg.averageCost", messages.money(summary.averageCost())),
                messages.format("report.msg.costValue", messages.money(summary.costValue())),
                messages.format("report.msg.marketValue",
                        messages.money(summary.marketValue()),
                        messages.money(summary.quote().price()),
                        messages.date(summary.quote().date())));
        render(messages.get("menu.report.3"), lines);
    }

    /**
     * Relatorio 4: historico de movimentacao, dia a dia.
     *
     * <p>As colunas acompanham a planilha de apuracao, de modo que o resultado
     * possa ser conferido linha a linha contra ela.</p>
     */
    public void showHistory(Wallet wallet, List<Transaction> transactions, WalletResult result) {
        List<String> lines = new ArrayList<>();
        lines.add(messages.format("report.msg.walletLine", wallet.getId(), wallet.getHolderName()));
        lines.add(RULE);
        if (transactions.isEmpty()) {
            lines.add(messages.get("report.msg.noTransactions"));
            render(messages.get("menu.report.4"), lines);
            return;
        }

        lines.add(String.format(messages.locale(), "%-12s %-6s %12s %14s %14s",
                messages.get("report.header.date"),
                messages.get("report.header.type"),
                messages.get("report.header.quantity"),
                messages.get("report.header.price"),
                messages.get("report.header.amount")));
        lines.add(RULE);
        for (Transaction transaction : transactions) {
            BigDecimal price = priceOn(result, transaction);
            lines.add(String.format(messages.locale(), "%-12s %-6s %12s %14s %14s",
                    messages.date(transaction.getDate()),
                    transaction.getType().code(),
                    messages.quantity(transaction.getQuantity()),
                    price == null ? "-" : messages.money(price),
                    price == null ? "-" : messages.money(transaction.getQuantity().multiply(price))));
        }

        lines.add("");
        lines.add(String.format(messages.locale(), "%-12s %12s %12s %14s %14s",
                messages.get("report.header.date"),
                messages.get("report.header.balance"),
                messages.get("report.header.avgCost"),
                messages.get("report.header.operationResult"),
                messages.get("report.header.accumulated")));
        lines.add(RULE);
        for (DailyPosition day : result.days()) {
            lines.add(String.format(messages.locale(), "%-12s %12s %12s %14s %14s",
                    messages.date(day.date()),
                    messages.quantity(day.balance()),
                    messages.money(day.averageCost()),
                    messages.money(day.operationResult()),
                    messages.money(day.accumulatedResult())));
        }
        render(messages.get("menu.report.4"), lines);
    }

    /** Relatorio 5: ganho ou perda total de cada carteira. */
    public void showResults(Map<Wallet, WalletResult> results) {
        List<String> lines = new ArrayList<>();
        if (results.isEmpty()) {
            lines.add(messages.get("report.msg.noWallets"));
        }
        for (Map.Entry<Wallet, WalletResult> entry : results.entrySet()) {
            Wallet wallet = entry.getKey();
            WalletResult result = entry.getValue();
            lines.add(messages.format("report.msg.walletLine", wallet.getId(), wallet.getHolderName()));
            lines.add(messages.format("report.msg.totalPurchases",
                    messages.money(result.totalPurchases())));
            lines.add(messages.format("report.msg.totalSales",
                    messages.money(result.totalSales())));
            lines.add(messages.format("report.msg.balance", messages.quantity(result.balance())));
            lines.add(messages.format("report.msg.costValue", messages.money(result.costBalance())));
            lines.add(messages.format("report.msg.marketValue2",
                    messages.money(result.marketBalance())));
            lines.add(resultLine(result.accumulatedResult()));
            lines.add(RULE);
        }
        render(messages.get("menu.report.5"), lines);
    }

    /** Lucro em verde, prejuizo em vermelho, zero em amarelo. */
    private String resultLine(BigDecimal accumulated) {
        String key = switch (Integer.signum(accumulated.signum())) {
            case 1 -> "report.msg.profit";
            case -1 -> "report.msg.loss";
            default -> "report.msg.breakEven";
        };
        return messages.format(key, messages.money(accumulated.abs()));
    }

    /** Cotacao do dia da movimentacao, tirada da apuracao ja calculada. */
    private BigDecimal priceOn(WalletResult result, Transaction transaction) {
        for (DailyPosition day : result.days()) {
            if (day.date().equals(transaction.getDate())) {
                return day.price();
            }
        }
        return null;
    }

    private void render(String title, List<String> lines) {
        Console.title(title);
        for (String line : lines) {
            System.out.println(line);
        }
        offerPrinting(title, String.join(System.lineSeparator(), lines));
    }

    /**
     * Oferece a impressao do relatorio recem-exibido.
     *
     * <p>Sem impressora instalada, grava em arquivo de texto: a opcao continua
     * util em laboratorio e na gravacao do video.</p>
     */
    private void offerPrinting(String title, String content) {
        if (!confirm(messages.get("report.print.ask"))) {
            return;
        }
        String document = title + System.lineSeparator()
                + RULE + System.lineSeparator()
                + content + System.lineSeparator();

        List<PrintService> printers = ReportPrinter.availablePrinters();
        if (printers.isEmpty()) {
            Console.warn(messages.get("report.print.none"));
            saveToFile(document);
            return;
        }
        for (int index = 0; index < printers.size(); index++) {
            System.out.println((index + 1) + " - " + printers.get(index).getName());
        }
        int choice = readInt("report.print.choose");
        if (choice < 1 || choice > printers.size()) {
            showInvalidOption();
            return;
        }
        try {
            ReportPrinter.printTo(printers.get(choice - 1), document);
            Console.success(messages.get("report.print.sent"));
        } catch (PrintException e) {
            Console.error(messages.get("report.print.failed"));
            saveToFile(document);
        }
    }

    private int countLines(String document) {
        return document.isEmpty() ? 0 : document.split("\r?\n", -1).length;
    }

    private void saveToFile(String document) {
        try {
            Path file = ReportPrinter.saveToFile("ftcoin-report.txt", document);
            ActivityLog.getInstance().reportExported(file, countLines(document));
            Console.success(messages.format("report.print.saved", file.toString()));
        } catch (IOException e) {
            Console.error(messages.get("report.print.failed"));
        }
    }
}
