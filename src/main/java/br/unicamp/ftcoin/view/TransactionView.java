package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.dto.Quote;
import br.unicamp.ftcoin.dto.TransactionReceipt;
import br.unicamp.ftcoin.util.Console;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Tela de movimentacao: compra, venda e cotacao. */
public class TransactionView extends BaseView {

    public int showMenu() {
        header("menu.transaction.title");
        for (int option = 1; option <= 3; option++) {
            System.out.println(messages.get("menu.transaction." + option));
        }
        System.out.println(messages.get("menu.transaction.0"));
        return readInt("menu.main.choose");
    }

    public int readWalletId() {
        return readInt("transaction.input.walletId");
    }

    public BigDecimal readQuantity() {
        return readDecimal("transaction.input.quantity");
    }

    public LocalDate readDate() {
        return readDate("transaction.input.date");
    }

    /** Comprovante da operacao, com a cotacao que foi efetivamente usada. */
    public void showReceipt(TransactionReceipt receipt, boolean bought) {
        Console.success(messages.format(
                bought ? "transaction.msg.bought" : "transaction.msg.sold",
                receipt.transaction().getId(),
                messages.quantity(receipt.transaction().getQuantity())));
        Console.info(messages.format("transaction.msg.quoteUsed",
                messages.money(receipt.quote().price()),
                messages.date(receipt.quote().date())));
        Console.info(messages.format("transaction.msg.amount", messages.money(receipt.amount())));
        Console.info(messages.format("transaction.msg.newBalance",
                messages.quantity(receipt.newBalance())));
    }

    /** Resposta da opcao "cotar moeda virtual". */
    public void showQuote(Quote quote) {
        Console.info(messages.format("oracle.msg.quoteOn",
                messages.date(quote.date()), messages.money(quote.price())));
    }
}
