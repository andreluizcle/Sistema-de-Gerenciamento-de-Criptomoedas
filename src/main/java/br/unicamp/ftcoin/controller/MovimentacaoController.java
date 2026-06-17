package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.dto.Movimentacao;
import br.unicamp.ftcoin.dto.Oraculo;
import br.unicamp.ftcoin.service.CarteiraService;
import br.unicamp.ftcoin.service.MovimentacaoService;
import br.unicamp.ftcoin.service.OraculoService;
import br.unicamp.ftcoin.view.MovimentacaoView;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Controller que orquestra a tela de Movimentacao
 * (compra e venda de moeda virtual).
 */
public class MovimentacaoController {

    private final MovimentacaoView view;
    private final MovimentacaoService movService;
    private final CarteiraService carteiraService;
    private final OraculoService oraculoService;

    public MovimentacaoController(MovimentacaoView view,
                                  MovimentacaoService movService,
                                  CarteiraService carteiraService,
                                  OraculoService oraculoService) {
        this.view = view;
        this.movService = movService;
        this.carteiraService = carteiraService;
        this.oraculoService = oraculoService;
    }

    public void executar() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.exibirMenu();
            switch (opcao) {
                case 1:
                    comprar();
                    break;
                case 2:
                    vender();
                    break;
                case 0:
                    voltar = true;
                    break;
                default:
                    view.mensagemErro("geral.opcao.invalida");
                    break;
            }
        }
    }

    private void comprar() {
        int idCarteira = view.lerIdCarteira();
        if (!carteiraService.existe(idCarteira)) {
            view.mensagemErro("carteira.msg.nao.encontrada");
            return;
        }
        BigDecimal qtd = view.lerQuantidade();
        if (qtd == null || qtd.signum() <= 0) {
            view.mensagemQuantidadeInvalida();
            return;
        }
        LocalDate data = view.lerData();
        if (data == null) {
            view.mensagemErro("mov.msg.quantidade.invalida");
            return;
        }
        Oraculo cot = oraculoService.cotacaoEm(data);
        view.exibirCotacaoUsada(cot);
        Movimentacao mov = movService.registrarCompra(idCarteira, qtd, data);
        if (mov == null) {
            view.mensagemQuantidadeInvalida();
        } else {
            view.mensagemCompraOk(mov);
        }
    }

    private void vender() {
        int idCarteira = view.lerIdCarteira();
        if (!carteiraService.existe(idCarteira)) {
            view.mensagemErro("carteira.msg.nao.encontrada");
            return;
        }
        BigDecimal qtd = view.lerQuantidade();
        if (qtd == null || qtd.signum() <= 0) {
            view.mensagemQuantidadeInvalida();
            return;
        }
        BigDecimal saldo = movService.calcularSaldo(idCarteira);
        if (saldo.compareTo(qtd) < 0) {
            view.mensagemSaldoInsuficiente(saldo);
            return;
        }
        LocalDate data = view.lerData();
        if (data == null) {
            view.mensagemErro("mov.msg.quantidade.invalida");
            return;
        }
        Oraculo cot = oraculoService.cotacaoEm(data);
        view.exibirCotacaoUsada(cot);
        Movimentacao mov = movService.registrarVenda(idCarteira, qtd, data);
        if (mov == null) {
            view.mensagemSaldoInsuficiente(saldo);
        } else {
            view.mensagemVendaOk(mov);
        }
    }
}
