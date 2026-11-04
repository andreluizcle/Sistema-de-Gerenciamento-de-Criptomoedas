package br.unicamp.ftcoin.controller;

import br.unicamp.ftcoin.dao.DAOFactory;
import br.unicamp.ftcoin.oracle.FormulaQuoteSource;
import br.unicamp.ftcoin.oracle.QuoteSource;
import br.unicamp.ftcoin.service.OracleService;
import br.unicamp.ftcoin.service.ReportService;
import br.unicamp.ftcoin.service.TransactionService;
import br.unicamp.ftcoin.service.WalletService;

/**
 * Monta os servicos e entrega os controladores sob demanda.
 *
 * <p>Os controladores sao criados apenas no primeiro uso. Quem abre o programa e
 * sai pelo menu principal nao paga pela construcao das telas de relatorio e de
 * ajuda, e toda a inicializacao preguicosa fica reunida em um unico lugar, em
 * vez de espalhada pelo metodo de partida.</p>
 *
 * <p>Os servicos, ao contrario, sao criados de imediato: nao tem estado nem
 * custo de construcao, e e por eles que os controladores sao parametrizados.</p>
 */
public final class ControllerFactory {

    private final WalletService walletService;
    private final TransactionService transactionService;
    private final ReportService reportService;

    private WalletController walletController;
    private TransactionController transactionController;
    private ReportController reportController;
    private HelpController helpController;

    public ControllerFactory(DAOFactory daoFactory) {
        this(daoFactory, new FormulaQuoteSource());
    }

    /**
     * Permite trocar a origem das cotacoes sem tocar em mais nada — e por aqui
     * que a API propria entra quando o contrato dela existir.
     */
    public ControllerFactory(DAOFactory daoFactory, QuoteSource quoteSource) {
        OracleService oracleService = new OracleService(quoteSource);
        this.walletService = new WalletService(daoFactory.walletDAO(), daoFactory.transactionDAO());
        this.transactionService = new TransactionService(daoFactory.transactionDAO(), oracleService);
        this.reportService = new ReportService(transactionService, oracleService);
    }

    public WalletController walletController() {
        if (walletController == null) {
            walletController = new WalletController(walletService, reportService);
        }
        return walletController;
    }

    public TransactionController transactionController() {
        if (transactionController == null) {
            transactionController = new TransactionController(transactionService, walletService);
        }
        return transactionController;
    }

    public ReportController reportController() {
        if (reportController == null) {
            reportController = new ReportController(reportService, walletService);
        }
        return reportController;
    }

    public HelpController helpController() {
        if (helpController == null) {
            helpController = new HelpController();
        }
        return helpController;
    }
}
