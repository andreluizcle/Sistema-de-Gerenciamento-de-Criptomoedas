package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.util.ActivityLog;
import java.nio.file.Path;
import br.unicamp.ftcoin.util.Console;

/** Tela de ajuda e creditos. */
public class HelpView extends BaseView {

    public int showMenu() {
        header("menu.help.title");
        System.out.println(messages.get("menu.help.1"));
        System.out.println(messages.get("menu.help.2"));
        System.out.println(messages.get("menu.help.3"));
        System.out.println(messages.get("menu.help.0"));
        return readInt("menu.main.choose");
    }

    public void showHelpText() {
        Console.title(messages.get("menu.help.title"));
        System.out.println(messages.get("help.text"));
        pause();
    }

    /**
     * Estado da sessao: metricas acumuladas e onde esta o registro de execucao.
     *
     * <p>Torna o monitoramento visivel dentro do proprio programa, sem depender
     * de acompanhar o arquivo por fora. A linha de metricas e mostrada como e
     * gravada no arquivo, para que as duas leituras possam ser comparadas.</p>
     */
    public void showSessionState() {
        ActivityLog log = ActivityLog.getInstance();
        Console.title(messages.get("menu.help.3"));
        System.out.println(messages.get("help.session.metrics"));
        System.out.println("  " + log.metricsLine());
        Path logFile = log.file();
        if (logFile == null) {
            Console.warn(messages.get("help.session.noFile"));
        } else {
            Console.info(messages.format("help.session.file", logFile.toAbsolutePath()));
        }
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
