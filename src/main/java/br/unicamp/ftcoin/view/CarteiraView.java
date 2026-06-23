package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.dto.Carteira;
import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.MessageProvider;

/**
 * View responsavel pela tela de Carteira e suas interacoes via CLI.
 */
public class CarteiraView extends ViewBase {

    public int exibirMenu() {
        cabecalho("menu.carteira.titulo");
        System.out.println(MessageProvider.get("menu.carteira.1"));
        System.out.println(MessageProvider.get("menu.carteira.2"));
        System.out.println(MessageProvider.get("menu.carteira.3"));
        System.out.println(MessageProvider.get("menu.carteira.4"));
        System.out.println(MessageProvider.get("menu.carteira.0"));
        return lerInteiro("menu.principal.escolha");
    }

    /**
     * Le os dados de uma nova carteira (sem ID, gerado por AUTO_INCREMENT).
     */
    public Carteira lerDadosCarteira() {
        String nome = lerLinha("carteira.entrada.nome");
        String corretora = lerLinha("carteira.entrada.corretora");
        if (nome.isEmpty() || corretora.isEmpty()) {
            return null;
        }
        return new Carteira(0, nome, corretora);
    }

    public int lerIdConsulta() {
        return lerInteiro("carteira.entrada.id.consulta");
    }

    public int lerIdEdicao() {
        return lerInteiro("carteira.entrada.id.editar");
    }

    public int lerIdExclusao() {
        return lerInteiro("carteira.entrada.id.excluir");
    }

    public String lerNovoNome() {
        return lerLinha("carteira.entrada.nome.novo");
    }

    public String lerNovaCorretora() {
        return lerLinha("carteira.entrada.corretora.nova");
    }

    public boolean confirmarExclusao(int id) {
        return confirmar(MessageProvider.get("carteira.msg.excluir.confirma", id));
    }

    public void exibirCarteira(Carteira c) {
        Console.info(MessageProvider.get("carteira.msg.encontrada") + " " + c.toString());
    }

    /**
     * Exibe os dados cadastrais da carteira + saldo em moeda virtual,
     * valor em moeda real e cotacao usada na conversao.
     */
    public void exibirCarteiraCompleta(Carteira c,
                                       java.math.BigDecimal saldo,
                                       java.math.BigDecimal valorReal,
                                       br.unicamp.ftcoin.dto.Oraculo cotacao) {
        Console.info(MessageProvider.get("carteira.msg.encontrada") + " " + c.toString());
        Console.info(MessageProvider.get("carteira.msg.saldo",
                saldo.setScale(8, java.math.RoundingMode.HALF_UP).toPlainString()));
        if (cotacao != null) {
            Console.info(MessageProvider.get("carteira.msg.valor.atual",
                    valorReal.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                    cotacao.getCotacao().toPlainString(),
                    cotacao.getData().toString()));
        } else {
            Console.aviso(MessageProvider.get("oraculo.cotacao.indisponivel"));
        }
    }

    public void mensagemSucesso(String chave) {
        Console.sucesso(MessageProvider.get(chave));
    }

    public void mensagemErro(String chave) {
        Console.erro(MessageProvider.get(chave));
    }
}
