package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.dto.Movimentacao;
import br.unicamp.ftcoin.dto.Oraculo;
import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.MessageProvider;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * View responsavel pela tela de Movimentacao (compra / venda).
 */
public class MovimentacaoView extends ViewBase {

    public int exibirMenu() {
        cabecalho("menu.movimentacao.titulo");
        System.out.println(MessageProvider.get("menu.movimentacao.1"));
        System.out.println(MessageProvider.get("menu.movimentacao.2"));
        System.out.println(MessageProvider.get("menu.movimentacao.0"));
        return lerInteiro("menu.principal.escolha");
    }

    public int lerIdCarteira() {
        return lerInteiro("mov.entrada.id.carteira");
    }

    public BigDecimal lerQuantidade() {
        return lerDecimal("mov.entrada.quantidade");
    }

    public LocalDate lerData() {
        return lerData("mov.entrada.data");
    }

    public void mensagemCompraOk(Movimentacao mov) {
        Console.sucesso(MessageProvider.get("mov.msg.compra.ok", mov.getIdMovimento()));
    }

    public void mensagemVendaOk(Movimentacao mov) {
        Console.sucesso(MessageProvider.get("mov.msg.venda.ok", mov.getIdMovimento()));
    }

    public void mensagemSaldoInsuficiente(BigDecimal saldo) {
        Console.erro(MessageProvider.get("mov.msg.saldo.insuficiente", saldo.toPlainString()));
    }

    public void mensagemQuantidadeInvalida() {
        Console.erro(MessageProvider.get("mov.msg.quantidade.invalida"));
    }

    public void exibirCotacaoUsada(Oraculo cot) {
        if (cot == null) {
            Console.aviso(MessageProvider.get("oraculo.cotacao.indisponivel"));
            return;
        }
        Console.info(MessageProvider.get("mov.msg.cotacao.usada",
                cot.getCotacao().toPlainString() + " (" + cot.getData() + ")"));
    }

    public void mensagemErro(String chave) {
        Console.erro(MessageProvider.get(chave));
    }
}
