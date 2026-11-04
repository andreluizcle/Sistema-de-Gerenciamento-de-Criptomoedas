package br.unicamp.ftcoin;

import br.unicamp.ftcoin.controller.ControllerFactory;
import br.unicamp.ftcoin.controller.MainMenuController;
import br.unicamp.ftcoin.dao.DAOFactory;
import br.unicamp.ftcoin.dao.PersistenceType;
import br.unicamp.ftcoin.dao.RelationalDAOFactory;
import br.unicamp.ftcoin.util.FtCoinException;
import br.unicamp.ftcoin.util.MessageProvider;
import br.unicamp.ftcoin.view.StartupView;

/**
 * Ponto de entrada do FT-Coin.
 *
 * <p>Faz apenas o necessario para a aplicacao existir: escolhe o idioma, escolhe
 * a persistencia, monta a fabrica e inicia o menu. As perguntas ao usuario estao
 * na tela de partida e a construcao dos objetos esta na fabrica de
 * controladores; o {@code try}-com-recursos garante que a conexao com o banco
 * seja fechada na saida, inclusive se algo falhar no meio.</p>
 */
public final class Start {

    private Start() {
    }

    public static void main(String[] args) {
        StartupView startup = new StartupView();
        MessageProvider.getInstance().setLocale(startup.askLanguage());
        PersistenceType persistence = startup.askPersistence();

        try (DAOFactory daoFactory = DAOFactory.of(persistence)) {
            if (daoFactory instanceof RelationalDAOFactory relational) {
                startup.showDatabaseInUse(relational.databaseUrl());
            }
            new MainMenuController(new ControllerFactory(daoFactory)).run();
        } catch (FtCoinException e) {
            startup.showFatalError(e.getMessageKey());
        }
    }
}
