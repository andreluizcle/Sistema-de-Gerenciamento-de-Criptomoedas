package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.util.ActivityLog;
import br.unicamp.ftcoin.util.FtCoinException;
import br.unicamp.ftcoin.view.MainMenuView;

/** Laco do menu principal; despacha para os demais controladores. */
public class MainMenuController {

    private final ControllerFactory controllers;
    private final MainMenuView view = new MainMenuView();

    public MainMenuController(ControllerFactory controllers) {
        this.controllers = controllers;
    }

    public void run() {
        view.showBanner();
        boolean exit = false;
        while (!exit) {
            try {
                exit = handle(view.showMenu());
            } catch (FtCoinException e) {
                ActivityLog.getInstance().errorCaught(e);
                view.showError(e.getMessageKey());
            }
        }
        view.showGoodbye();
    }

    private boolean handle(int option) {
        switch (option) {
            case 1 -> controllers.walletController().run();
            case 2 -> controllers.transactionController().run();
            case 3 -> controllers.reportController().run();
            case 4 -> controllers.helpController().run();
            case 5 -> {
                return true;
            }
            default -> view.showInvalidOption();
        }
        return false;
    }
}
