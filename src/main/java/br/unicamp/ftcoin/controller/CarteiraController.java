package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.dto.Carteira;
import br.unicamp.ftcoin.dto.Oraculo;
import br.unicamp.ftcoin.service.CarteiraService;
import br.unicamp.ftcoin.service.MovimentacaoService;
import br.unicamp.ftcoin.service.OraculoService;
import br.unicamp.ftcoin.view.CarteiraView;

import java.math.BigDecimal;

/**
 * Controller que orquestra as chamadas entre CarteiraView e CarteiraService.
 * Conforme a revisao arquitetural (Sumario Executivo II), o controller apenas
 * coordena a chamada de metodos, sem regras de negocio proprias.
 */
public class CarteiraController {

    private final CarteiraView view;
    private final CarteiraService service;
    private final MovimentacaoService movimentacaoService;
    private final OraculoService oraculoService;

    public CarteiraController(CarteiraView view,
                              CarteiraService service,
                              MovimentacaoService movimentacaoService,
                              OraculoService oraculoService) {
        this.view = view;
        this.service = service;
        this.movimentacaoService = movimentacaoService;
        this.oraculoService = oraculoService;
    }

    public void executar() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.exibirMenu();
            switch (opcao) {
                case 1:
                    incluir();
                    break;
                case 2:
                    consultar();
                    break;
                case 3:
                    editar();
                    break;
                case 4:
                    excluir();
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

    private void incluir() {
        Carteira nova = view.lerDadosCarteira();
        if (nova == null) {
            view.mensagemErro("carteira.msg.invalida");
            return;
        }
        if (service.existe(nova.getId())) {
            view.mensagemErro("carteira.msg.id.existente");
            return;
        }
        if (service.incluir(nova)) {
            view.mensagemSucesso("carteira.msg.incluida");
        } else {
            view.mensagemErro("carteira.msg.invalida");
        }
    }

    private void consultar() {
        int id = view.lerIdConsulta();
        Carteira c = service.consultar(id);
        if (c == null) {
            view.mensagemErro("carteira.msg.nao.encontrada");
            return;
        }
        BigDecimal saldo = movimentacaoService.calcularSaldo(id);
        BigDecimal valorReal = movimentacaoService.valorAtual(id);
        Oraculo cot = oraculoService.cotacaoMaisRecente();
        view.exibirCarteiraCompleta(c, saldo, valorReal, cot);
    }

    private void editar() {
        int id = view.lerIdEdicao();
        Carteira c = service.consultar(id);
        if (c == null) {
            view.mensagemErro("carteira.msg.nao.encontrada");
            return;
        }
        view.exibirCarteira(c);
        String novoNome = view.lerNovoNome();
        String novaCorretora = view.lerNovaCorretora();
        if (!novoNome.isEmpty()) {
            c.setNomeTitular(novoNome);
        }
        if (!novaCorretora.isEmpty()) {
            c.setCorretora(novaCorretora);
        }
        if (service.atualizar(c)) {
            view.mensagemSucesso("carteira.msg.atualizada");
        } else {
            view.mensagemErro("carteira.msg.invalida");
        }
    }

    private void excluir() {
        int id = view.lerIdExclusao();
        Carteira c = service.consultar(id);
        if (c == null) {
            view.mensagemErro("carteira.msg.nao.encontrada");
            return;
        }
        view.exibirCarteira(c);
        if (!view.confirmarExclusao(id)) {
            return;
        }
        if (service.excluir(id)) {
            view.mensagemSucesso("carteira.msg.excluida");
        } else {
            view.mensagemErro("carteira.msg.invalida");
        }
    }
}
