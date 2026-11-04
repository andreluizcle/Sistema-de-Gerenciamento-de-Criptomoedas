package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.view.HelpView;

/**
 * Orquestra o submenu de ajuda.
 *
 * <p>Nao herda de nenhuma classe de tela: na versao anterior este controlador
 * estendia a base das views so para reaproveitar um metodo, misturando as
 * camadas de controle e apresentacao.</p>
 */
public class HelpController {

    private final HelpView view = new HelpView();

    public void run() {
        boolean back = false;
        while (!back) {
            switch (view.showMenu()) {
                case 1 -> view.showHelpText();
                case 2 -> view.showCredits();
                case 3 -> view.showSessionState();
                case 0 -> back = true;
                default -> view.showInvalidOption();
            }
        }
    }
}
