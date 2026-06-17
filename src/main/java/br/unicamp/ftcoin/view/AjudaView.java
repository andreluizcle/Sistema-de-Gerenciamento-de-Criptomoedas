package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.MessageProvider;

/**
 * View para o submenu de Ajuda e creditos.
 */
public class AjudaView extends ViewBase {

    public int exibirMenu() {
        cabecalho("menu.ajuda.titulo");
        System.out.println(MessageProvider.get("menu.ajuda.1"));
        System.out.println(MessageProvider.get("menu.ajuda.2"));
        System.out.println(MessageProvider.get("menu.ajuda.0"));
        return lerInteiro("menu.principal.escolha");
    }

    public void exibirTextoAjuda() {
        Console.titulo(MessageProvider.get("menu.ajuda.titulo"));
        System.out.println(MessageProvider.get("ajuda.texto"));
    }

    public void exibirCreditos() {
        Console.titulo(MessageProvider.get("ajuda.creditos.titulo"));
        System.out.println(MessageProvider.get("app.titulo"));
        System.out.println(MessageProvider.get("ajuda.creditos.versao",
                MessageProvider.get("app.versao")));
        System.out.println(MessageProvider.get("ajuda.creditos.data",
                MessageProvider.get("app.data")));
        System.out.println(MessageProvider.get("ajuda.creditos.copyright",
                MessageProvider.get("app.copyright")));
        System.out.println(MessageProvider.get("ajuda.creditos.autores",
                MessageProvider.get("app.autores")));
        System.out.println(MessageProvider.get("ajuda.creditos.membros"));
    }
}
