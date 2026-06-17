package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.view.AjudaView;
import br.unicamp.ftcoin.view.ViewBase;

/**
 * Controller para o submenu de Ajuda.
 */
public class AjudaController extends ViewBase {

    private final AjudaView view;

    public AjudaController(AjudaView view) {
        this.view = view;
    }

    public void executar() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.exibirMenu();
            switch (opcao) {
                case 1:
                    view.exibirTextoAjuda();
                    break;
                case 2:
                    view.exibirCreditos();
                    break;
                case 0:
                    voltar = true;
                    break;
                default:
                    mostrarOpcaoInvalida();
                    break;
            }
        }
    }
}
