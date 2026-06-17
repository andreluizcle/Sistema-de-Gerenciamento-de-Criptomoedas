package br.unicamp.ftcoin;

import br.unicamp.ftcoin.controller.AjudaController;
import br.unicamp.ftcoin.controller.CarteiraController;
import br.unicamp.ftcoin.controller.MenuPrincipalController;
import br.unicamp.ftcoin.controller.MovimentacaoController;
import br.unicamp.ftcoin.controller.RelatorioController;
import br.unicamp.ftcoin.dao.interfaces.ICarteiraDAO;
import br.unicamp.ftcoin.dao.interfaces.IMovimentacaoDAO;
import br.unicamp.ftcoin.dao.interfaces.IOraculoDAO;
import br.unicamp.ftcoin.dao.memory.CarteiraMemoryDAO;
import br.unicamp.ftcoin.dao.memory.MovimentacaoMemoryDAO;
import br.unicamp.ftcoin.dao.memory.OraculoMemoryDAO;
import br.unicamp.ftcoin.dao.relational.CarteiraRelationalDAO;
import br.unicamp.ftcoin.dao.relational.MovimentacaoRelationalDAO;
import br.unicamp.ftcoin.dao.relational.OraculoRelationalDAO;
import br.unicamp.ftcoin.service.CarteiraService;
import br.unicamp.ftcoin.service.MovimentacaoService;
import br.unicamp.ftcoin.service.OraculoService;
import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.MessageProvider;
import br.unicamp.ftcoin.view.AjudaView;
import br.unicamp.ftcoin.view.CarteiraView;
import br.unicamp.ftcoin.view.MenuPrincipalView;
import br.unicamp.ftcoin.view.MovimentacaoView;
import br.unicamp.ftcoin.view.RelatorioView;
import br.unicamp.ftcoin.view.ViewBase;

import java.util.Locale;

/**
 * Ponto de entrada da aplicacao FT-Coin.
 * Responsavel por:
 *   1) Solicitar idioma (pt_BR ou en_US);
 *   2) Solicitar o modo de persistencia (memoria ou MariaDB);
 *   3) Montar (injetar) os DAOs, Services, Views e Controllers;
 *   4) Iniciar o menu principal.
 */
public final class Start {

    private Start() {
    }

    public static void main(String[] args) {
        selecionarIdioma();
        boolean usarRelacional = selecionarPersistencia();

        // --- DAOs --------------------------------------------------------
        ICarteiraDAO carteiraDAO;
        IMovimentacaoDAO movimentacaoDAO;
        IOraculoDAO oraculoDAO;
        if (usarRelacional) {
            carteiraDAO = new CarteiraRelationalDAO();
            movimentacaoDAO = new MovimentacaoRelationalDAO();
            oraculoDAO = new OraculoRelationalDAO();
        } else {
            carteiraDAO = new CarteiraMemoryDAO();
            movimentacaoDAO = new MovimentacaoMemoryDAO();
            oraculoDAO = new OraculoMemoryDAO();
        }

        // --- Services ----------------------------------------------------
        OraculoService oraculoService = new OraculoService(oraculoDAO);
        CarteiraService carteiraService = new CarteiraService(carteiraDAO, movimentacaoDAO);
        MovimentacaoService movimentacaoService = new MovimentacaoService(movimentacaoDAO, oraculoService);

        // --- Views -------------------------------------------------------
        MenuPrincipalView menuPrincipalView = new MenuPrincipalView();
        CarteiraView carteiraView = new CarteiraView();
        MovimentacaoView movimentacaoView = new MovimentacaoView();
        RelatorioView relatorioView = new RelatorioView();
        AjudaView ajudaView = new AjudaView();

        // --- Controllers -------------------------------------------------
        CarteiraController carteiraController = new CarteiraController(
                carteiraView, carteiraService, movimentacaoService, oraculoService);
        MovimentacaoController movimentacaoController = new MovimentacaoController(
                movimentacaoView, movimentacaoService, carteiraService, oraculoService);
        RelatorioController relatorioController = new RelatorioController(
                relatorioView, carteiraService, movimentacaoService, oraculoService);
        AjudaController ajudaController = new AjudaController(ajudaView);

        MenuPrincipalController menuPrincipalController = new MenuPrincipalController(
                menuPrincipalView,
                carteiraController,
                movimentacaoController,
                relatorioController,
                ajudaController
        );

        menuPrincipalController.executar();
    }

    private static void selecionarIdioma() {
        System.out.println(MessageProvider.get("inicio.selecione.idioma"));
        System.out.println(MessageProvider.get("inicio.idioma.pt"));
        System.out.println(MessageProvider.get("inicio.idioma.en"));
        System.out.print("> ");
        String entrada = ViewBase.scanner.nextLine().trim();
        if ("2".equals(entrada)) {
            MessageProvider.setLocale(new Locale("en", "US"));
        } else {
            MessageProvider.setLocale(new Locale("pt", "BR"));
        }
    }

    private static boolean selecionarPersistencia() {
        System.out.println(MessageProvider.get("inicio.selecione.persistencia"));
        System.out.println(MessageProvider.get("inicio.persistencia.memoria"));
        System.out.println(MessageProvider.get("inicio.persistencia.relacional"));
        System.out.print("> ");
        String entrada = ViewBase.scanner.nextLine().trim();
        if ("2".equals(entrada)) {
            Console.aviso("MariaDB - usando "
                    + br.unicamp.ftcoin.dao.relational.DatabaseConnection.getUrlPublica()
                    + " (credenciais via .env / variaveis de ambiente)");
            return true;
        }
        return false;
    }
}
