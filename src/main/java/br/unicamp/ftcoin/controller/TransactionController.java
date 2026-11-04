package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.dto.TransactionReceipt;
import br.unicamp.ftcoin.service.TransactionService;
import br.unicamp.ftcoin.service.WalletService;
import br.unicamp.ftcoin.util.FtCoinException;
import br.unicamp.ftcoin.view.TransactionView;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Orquestra compra, venda e cotacao da moeda virtual. */
public class TransactionController {

    private final TransactionService transactionService;
    private final WalletService walletService;
    private final TransactionView view = new TransactionView();

    public TransactionController(TransactionService transactionService, WalletService walletService) {
        this.transactionService = transactionService;
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
            case 1 -> trade(true);
            case 2 -> trade(false);
            case 3 -> quote();
            case 0 -> {
                return true;
            }
            default -> view.showInvalidOption();
        }
        return false;
    }

    private void trade(boolean buying) {
        int walletId = view.readWalletId();
        if (!walletService.exists(walletId)) {
            view.showError("wallet.msg.notFound");
            return;
        }
        BigDecimal quantity = view.readQuantity();
        if (quantity == null) {
            view.showError("transaction.msg.invalidQuantity");
            return;
        }
        LocalDate date = view.readDate();
        if (date == null) {
            view.showError("error.input.invalidDate");
            return;
        }
        TransactionReceipt receipt = buying
                ? transactionService.buy(walletId, quantity, date)
                : transactionService.sell(walletId, quantity, date);
        view.showReceipt(receipt, buying);
    }

    /** Cotar moeda virtual: a cotacao de qualquer data, sem registrar operacao. */
    private void quote() {
        LocalDate date = view.readDate();
        if (date == null) {
            view.showError("error.input.invalidDate");
            return;
        }
        view.showQuote(transactionService.quoteOn(date));
    }
}
