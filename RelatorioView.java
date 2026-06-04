package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.dto.Carteira;
import br.unicamp.ftcoin.dto.Movimentacao;
import br.unicamp.ftcoin.dto.Oraculo;
import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.MessageProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * View dos relatorios. Imprime listas em formato tabular usando
 * codigos ANSI para destacar cabecalhos e resultados positivos/negativos.
 */
public class RelatorioView extends ViewBase {

    public int exibirMenu() {
        cabecalho("menu.relatorios.titulo");
        System.out.println(MessageProvider.get("menu.relatorios.1"));
        System.out.println(MessageProvider.get("menu.relatorios.2"));
        System.out.println(MessageProvider.get("menu.relatorios.3"));
        System.out.println(MessageProvider.get("menu.relatorios.4"));
        System.out.println(MessageProvider.get("menu.relatorios.5"));
        System.out.println(MessageProvider.get("menu.relatorios.0"));
        return lerInteiro("menu.principal.escolha");
    }

    public int lerIdCarteira() {
        return lerInteiro("mov.entrada.id.carteira");
    }

    public void exibirListaCarteiras(List<Carteira> carteiras) {
        if (carteiras == null || carteiras.isEmpty()) {
            Console.aviso(MessageProvider.get("rel.msg.nenhuma.carteira"));
            return;
        }
        String cabId = MessageProvider.get("rel.cab.id");
        String cabTit = MessageProvider.get("rel.cab.titular");
        String cabCorr = MessageProvider.get("rel.cab.corretora");

        System.out.println(Console.colorir(Console.NEGRITO + Console.CIANO,
                String.format("%-6s %-30s %-20s", cabId, cabTit, cabCorr)));
        System.out.println(Console.colorir(Console.CIANO,
                "------------------------------------------------------------"));
        for (Carteira c : carteiras) {
            System.out.printf("%-6d %-30s %-20s%n",
                    c.getId(), c.getNomeTitular(), c.getCorretora());
        }
    }

    public void exibirSaldo(Carteira c, BigDecimal saldo, BigDecimal valorReal, Oraculo cotacao) {
        Console.info(MessageProvider.get("rel.msg.saldo",
                c.getId(), c.getNomeTitular(), saldo.setScale(8, RoundingMode.HALF_UP).toPlainString()));
        if (cotacao != null) {
            Console.info(MessageProvider.get("rel.msg.valor.atual",
                    valorReal.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    cotacao.getCotacao().toPlainString()));
        } else {
            Console.aviso(MessageProvider.get("oraculo.cotacao.indisponivel"));
        }
    }

    public void exibirHistorico(Carteira c, List<Movimentacao> movs) {
        if (movs == null || movs.isEmpty()) {
            Console.aviso(MessageProvider.get("rel.msg.nenhuma.mov"));
            return;
        }
        Console.info("Carteira " + c.getId() + " - " + c.getNomeTitular());
        String cabData = MessageProvider.get("rel.cab.data");
        String cabTipo = MessageProvider.get("rel.cab.tipo");
        String cabQtde = MessageProvider.get("rel.cab.quantidade");
        String cabMov = MessageProvider.get("rel.cab.id");

        System.out.println(Console.colorir(Console.NEGRITO + Console.CIANO,
                String.format("%-6s %-12s %-6s %-20s", cabMov, cabData, cabTipo, cabQtde)));
        System.out.println(Console.colorir(Console.CIANO,
                "--------------------------------------------"));
        for (Movimentacao m : movs) {
            System.out.printf("%-6d %-12s %-6c %-20s%n",
                    m.getIdMovimento(), m.getDataOperacao(),
                    m.getTipoOperacao(),
                    m.getQuantidade().toPlainString());
        }
    }

    public void exibirGanhoOuPerda(Carteira c, BigDecimal resultado) {
        String valor = resultado.setScale(2, RoundingMode.HALF_UP).abs().toPlainString();
        Console.info("Carteira " + c.getId() + " - " + c.getNomeTitular());
        int sinal = resultado.signum();
        if (sinal > 0) {
            System.out.println(Console.colorir(Console.VERDE,
                    MessageProvider.get("rel.msg.resultado.lucro", valor)));
        } else if (sinal < 0) {
            System.out.println(Console.colorir(Console.VERMELHO,
                    MessageProvider.get("rel.msg.resultado.prejuizo", valor)));
        } else {
            System.out.println(Console.colorir(Console.AMARELO,
                    MessageProvider.get("rel.msg.resultado.zero")));
        }
    }

    public void mensagemErro(String chave) {
        Console.erro(MessageProvider.get(chave));
    }

    public void mensagemAviso(String chave) {
        Console.aviso(MessageProvider.get(chave));
    }
}
