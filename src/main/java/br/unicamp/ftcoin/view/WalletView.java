package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.dto.Wallet;
import br.unicamp.ftcoin.dto.WalletSummary;
import br.unicamp.ftcoin.util.Console;

/** Tela de carteira: CRUD e consulta com saldo valorado. */
public class WalletView extends BaseView {

    public int showMenu() {
        header("menu.wallet.title");
        for (int option = 1; option <= 4; option++) {
            System.out.println(messages.get("menu.wallet." + option));
        }
        System.out.println(messages.get("menu.wallet.0"));
        return readInt("menu.main.choose");
    }

    /** Le os dados de uma carteira nova; o identificador vem do banco. */
    public Wallet readNewWallet() {
        String holder = readLine("wallet.input.holder");
        String brokerage = readLine("wallet.input.brokerage");
        return new Wallet(0, holder, brokerage);
    }

    public int readWalletId(String promptKey) {
        return readInt(promptKey);
    }

    public String readNewHolder() {
        return readLine("wallet.input.holder.new");
    }

    public String readNewBrokerage() {
        return readLine("wallet.input.brokerage.new");
    }

    public boolean confirmDelete(int id) {
        return confirm(messages.format("wallet.msg.deleteConfirm", id));
    }

    /**
     * Mostra os dados da carteira montando os rotulos a partir do bundle.
     *
     * <p>Nao usa {@code toString()} do DTO: aquele texto e tecnico e esta em um
     * idioma so, e era por ele que o portugues vazava para a tela em ingles.</p>
     */
    public void showWallet(Wallet wallet) {
        Console.info(messages.format("wallet.msg.found",
                wallet.getId(), wallet.getHolderName(), wallet.getBrokerage()));
    }

    /** Dados cadastrais mais saldo, custo medio e valor a mercado. */
    public void showSummary(WalletSummary summary) {
        showWallet(summary.wallet());
        Console.info(messages.format("wallet.msg.balance",
                messages.quantity(summary.balance())));
        Console.info(messages.format("wallet.msg.averageCost",
                messages.money(summary.averageCost())));
        Console.info(messages.format("wallet.msg.marketValue",
                messages.money(summary.marketValue()),
                messages.money(summary.quote().price()),
                messages.date(summary.quote().date())));
    }
}
