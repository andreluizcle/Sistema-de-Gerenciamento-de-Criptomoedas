package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.dto.Wallet;
import br.unicamp.ftcoin.dto.WalletResult;
import br.unicamp.ftcoin.service.ReportService;
import br.unicamp.ftcoin.service.WalletService;
import br.unicamp.ftcoin.util.FtCoinException;
import br.unicamp.ftcoin.view.ReportView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Orquestra os cinco relatorios exigidos pelo enunciado. */
public class ReportController {

    private final ReportService reportService;
    private final WalletService walletService;
    private final ReportView view = new ReportView();

    public ReportController(ReportService reportService, WalletService walletService) {
        this.reportService = reportService;
        this.walletService = walletService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            try {
                back = handle(view.showMenu());
            } catch (FtCoinException e) {
                view.showError(e.getMessageKey());
            }
        }
    }

    private boolean handle(int option) {
        switch (option) {
            case 1 -> view.showWalletList("menu.report.1", walletService.listById());
            case 2 -> view.showWalletList("menu.report.2", walletService.listByHolder());
            case 3 -> showBalance();
            case 4 -> showHistory();
            case 5 -> showResults();
            case 0 -> {
                return true;
            }
            default -> view.showInvalidOption();
        }
        return false;
    }

    private void showBalance() {
        Wallet wallet = walletService.findById(view.readWalletId());
        if (wallet == null) {
            view.showError("wallet.msg.notFound");
            return;
        }
        view.showSummary(reportService.summaryOf(wallet));
    }

    private void showHistory() {
        Wallet wallet = walletService.findById(view.readWalletId());
        if (wallet == null) {
            view.showError("wallet.msg.notFound");
            return;
        }
        WalletResult result = reportService.resultOf(wallet.getId());
        view.showHistory(wallet, reportService.historyOf(wallet.getId()), result);
    }

    private void showResults() {
        List<Wallet> wallets = walletService.listById();
        Map<Wallet, WalletResult> results = new LinkedHashMap<>();
        for (Wallet wallet : wallets) {
            results.put(wallet, reportService.resultOf(wallet.getId()));
        }
        view.showResults(results);
    }
}
