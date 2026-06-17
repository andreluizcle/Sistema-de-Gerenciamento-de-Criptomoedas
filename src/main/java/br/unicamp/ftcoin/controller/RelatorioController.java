package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.dto.Carteira;
import br.unicamp.ftcoin.dto.Movimentacao;
import br.unicamp.ftcoin.dto.Oraculo;
import br.unicamp.ftcoin.service.CarteiraService;
import br.unicamp.ftcoin.service.MovimentacaoService;
import br.unicamp.ftcoin.service.OraculoService;
import br.unicamp.ftcoin.view.RelatorioView;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller dos relatorios. Atende as cinco funcionalidades
 * exigidas no enunciado:
 *   1) Listar carteiras por identificador
 *   2) Listar carteiras por nome do titular
 *   3) Exibir saldo atual de uma carteira
 *   4) Exibir historico de movimentacao
 *   5) Apresentar ganho ou perda total de cada carteira
 */
public class RelatorioController {

    private final RelatorioView view;
    private final CarteiraService carteiraService;
    private final MovimentacaoService movService;
    private final OraculoService oraculoService;

    public RelatorioController(RelatorioView view,
                               CarteiraService carteiraService,
                               MovimentacaoService movService,
                               OraculoService oraculoService) {
        this.view = view;
        this.carteiraService = carteiraService;
        this.movService = movService;
        this.oraculoService = oraculoService;
    }

    public void executar() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.exibirMenu();
            switch (opcao) {
                case 1:
                    listarPorId();
                    break;
                case 2:
                    listarPorTitular();
                    break;
                case 3:
                    exibirSaldo();
                    break;
                case 4:
                    exibirHistorico();
                    break;
                case 5:
                    exibirGanhoPerda();
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

    private void listarPorId() {
        view.exibirListaCarteiras(carteiraService.listarOrdenadasPorId());
    }

    private void listarPorTitular() {
        view.exibirListaCarteiras(carteiraService.listarOrdenadasPorTitular());
    }

    private void exibirSaldo() {
        int id = view.lerIdCarteira();
        Carteira c = carteiraService.consultar(id);
        if (c == null) {
            view.mensagemErro("carteira.msg.nao.encontrada");
            return;
        }
        BigDecimal saldo = movService.calcularSaldo(id);
        BigDecimal valorReal = movService.valorAtual(id);
        Oraculo cot = oraculoService.cotacaoMaisRecente();
        view.exibirSaldo(c, saldo, valorReal, cot);
    }

    private void exibirHistorico() {
        int id = view.lerIdCarteira();
        Carteira c = carteiraService.consultar(id);
        if (c == null) {
            view.mensagemErro("carteira.msg.nao.encontrada");
            return;
        }
        List<Movimentacao> historico = movService.historico(id);
        view.exibirHistorico(c, historico);
    }

    private void exibirGanhoPerda() {
        List<Carteira> carteiras = carteiraService.listarOrdenadasPorId();
        if (carteiras.isEmpty()) {
            view.mensagemAviso("rel.msg.nenhuma.carteira");
            return;
        }
        for (Carteira c : carteiras) {
            BigDecimal resultado = movService.calcularGanhoOuPerda(c.getId());
            view.exibirGanhoOuPerda(c, resultado);
        }
    }
}
