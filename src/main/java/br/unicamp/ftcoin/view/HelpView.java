package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.util.Console;

/** Tela de ajuda e creditos. */
public class HelpView extends BaseView {

    public int showMenu() {
        header("menu.help.title");
        System.out.println(messages.get("menu.help.1"));
        System.out.println(messages.get("menu.help.2"));
        System.out.println(messages.get("menu.help.0"));
        return readInt("menu.main.choose");
    }

    public void showHelpText() {
        Console.title(messages.get("menu.help.title"));
        System.out.println(messages.get("help.text"));
        pause();
    }

    public void showCredits() {
        Console.title(messages.get("help.credits.title"));
        System.out.println(messages.get("app.title"));
        System.out.println(messages.format("help.credits.version", messages.get("app.version")));
        System.out.println(messages.format("help.credits.date", messages.get("app.date")));
        System.out.println(messages.format("help.credits.copyright", messages.get("app.copyright")));
        System.out.println(messages.format("help.credits.authors", messages.get("app.authors")));
        System.out.println(messages.get("help.credits.members"));
        pause();
    }
}
