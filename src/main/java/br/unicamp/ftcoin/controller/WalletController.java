package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.dto.Wallet;
import br.unicamp.ftcoin.service.ReportService;
import br.unicamp.ftcoin.service.WalletService;
import br.unicamp.ftcoin.util.FtCoinException;
import br.unicamp.ftcoin.view.WalletView;

/** Orquestra o CRUD de carteiras. */
public class WalletController {

    private final WalletService walletService;
    private final ReportService reportService;
    private final WalletView view = new WalletView();

    public WalletController(WalletService walletService, ReportService reportService) {
        this.walletService = walletService;
        this.reportService = reportService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            // Uma falha de banco ou de regra nao deve derrubar o menu: a mensagem
            // e exibida e o usuario continua de onde estava.
            try {
                back = handle(view.showMenu());
            } catch (FtCoinException e) {
                view.showError(e.getMessageKey());
            }
        }
    }

    private boolean handle(int option) {
        switch (option) {
            case 1 -> add();
            case 2 -> find();
            case 3 -> edit();
            case 4 -> delete();
            case 0 -> {
                return true;
            }
            default -> view.showInvalidOption();
        }
        return false;
    }

    private void add() {
        Wallet wallet = view.readNewWallet();
        if (walletService.add(wallet)) {
            view.showSuccess("wallet.msg.added");
            view.showWallet(wallet);
        } else {
            view.showError("wallet.msg.notAdded");
        }
    }

    private void find() {
        Wallet wallet = walletService.findById(view.readWalletId("wallet.input.id.find"));
        if (wallet == null) {
            view.showError("wallet.msg.notFound");
            return;
        }
        view.showSummary(reportService.summaryOf(wallet));
    }

    private void edit() {
        Wallet wallet = walletService.findById(view.readWalletId("wallet.input.id.edit"));
        if (wallet == null) {
            view.showError("wallet.msg.notFound");
            return;
        }
        view.showWallet(wallet);

        // Campo deixado em branco mantem o valor atual.
        String holder = view.readNewHolder();
        if (!holder.isEmpty()) {
            wallet.setHolderName(holder);
        }
        String brokerage = view.readNewBrokerage();
        if (!brokerage.isEmpty()) {
            wallet.setBrokerage(brokerage);
        }

        if (walletService.update(wallet)) {
            view.showSuccess("wallet.msg.updated");
        } else {
            view.showError("wallet.msg.notUpdated");
        }
    }

    private void delete() {
        int id = view.readWalletId("wallet.input.id.delete");
        if (!walletService.exists(id)) {
            view.showError("wallet.msg.notFound");
            return;
        }
        if (!view.confirmDelete(id)) {
            return;
        }
        if (walletService.delete(id)) {
            view.showSuccess("wallet.msg.deleted");
        } else {
            view.showError("wallet.msg.notDeleted");
        }
    }
}
