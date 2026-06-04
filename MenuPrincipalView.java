package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.MessageProvider;

//View do Menu Principal
public class MenuPrincipalView extends ViewBase {

    public void exibirCabecalho() {
        Console.limpar();
        System.out.println(Console.colorir(Console.NEGRITO + Console.AZUL,
                "*** " + MessageProvider.get("app.titulo") + " ***"));
    }

    //Exibe o menu principal e retorna a opcao escolhida
    public int exibirMenu() {
        cabecalho("menu.principal.titulo");
        System.out.println(MessageProvider.get("menu.principal.1"));
        System.out.println(MessageProvider.get("menu.principal.2"));
        System.out.println(MessageProvider.get("menu.principal.3"));
        System.out.println(MessageProvider.get("menu.principal.4"));
        System.out.println(MessageProvider.get("menu.principal.5"));
        return lerInteiro("menu.principal.escolha");
    }

    public void mensagemSaida() {
        Console.sucesso(MessageProvider.get("geral.saindo"));
    }

    //Versao publica para uso pelos controllers
    public void opcaoInvalida() {
        mostrarOpcaoInvalida();
    }
}
