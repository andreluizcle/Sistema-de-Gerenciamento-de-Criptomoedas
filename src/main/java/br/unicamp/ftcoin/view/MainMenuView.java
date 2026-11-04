package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.util.Console;

/** Menu raiz da aplicacao. */
public class MainMenuView extends BaseView {

    public void showBanner() {
        Console.clear();
        System.out.println(Console.paint(Console.BOLD + Console.BLUE,
                "*** " + messages.get("app.title") + " ***"));
    }

    public int showMenu() {
        header("menu.main.title");
        for (int option = 1; option <= 5; option++) {
            System.out.println(messages.get("menu.main." + option));
        }
        return readInt("menu.main.choose");
    }

    public void showGoodbye() {
        Console.success(messages.get("general.exiting"));
    }
}
