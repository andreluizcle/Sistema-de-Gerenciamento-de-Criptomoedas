package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.view.MenuPrincipalView;

/**
 * Controller principal: contem o loop de menu da aplicacao
 * e despacha o fluxo para os controllers de Carteira, Movimentacao,
 * Relatorios e Ajuda.
 */
public class MenuPrincipalController {

    private final MenuPrincipalView view;
    private final CarteiraController carteiraController;
    private final MovimentacaoController movimentacaoController;
    private final RelatorioController relatorioController;
    private final AjudaController ajudaController;

    public MenuPrincipalController(MenuPrincipalView view,
                                   CarteiraController carteiraController,
                                   MovimentacaoController movimentacaoController,
                                   RelatorioController relatorioController,
                                   AjudaController ajudaController) {
        this.view = view;
        this.carteiraController = carteiraController;
        this.movimentacaoController = movimentacaoController;
        this.relatorioController = relatorioController;
        this.ajudaController = ajudaController;
    }

    public void executar() {
        view.exibirCabecalho();
        boolean sair = false;
        while (!sair) {
            int opcao = view.exibirMenu();
            switch (opcao) {
                case 1:
                    carteiraController.executar();
                    break;
                case 2:
                    movimentacaoController.executar();
                    break;
                case 3:
                    relatorioController.executar();
                    break;
                case 4:
                    ajudaController.executar();
                    break;
                case 5:
                    sair = true;
                    break;
                default:
                    view.opcaoInvalida();
                    break;
            }
        }
        view.mensagemSaida();
    }

    /** Permite que outros componentes peguem a view, se necessario. */
    public MenuPrincipalView getView() {
        return view;
    }
}
