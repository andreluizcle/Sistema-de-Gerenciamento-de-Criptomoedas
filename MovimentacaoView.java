package java.view;

import java.MessageProvider;
import java.dto.Movimentacao;
import java.util.List;
import java.util.Scanner;

public class MovimentacaoView {
    private Scanner scanner;

    public MovimentacaoView() {
        this.scanner = new Scanner(System.in);
    }

    public int exibirMenu() {
        System.out.println("\n" + MessageProvider.get("transaction.title"));
        System.out.println(MessageProvider.get("transaction.buy"));
        System.out.println(MessageProvider.get("transaction.sell"));
        System.out.println(MessageProvider.get("transaction.list"));
        System.out.println("0. Voltar");
        System.out.print(MessageProvider.get("menu.option") + " ");
        
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int solicitarIdCarteira() {
        System.out.print(MessageProvider.get("transaction.wallet_id") + " ");
        return Integer.parseInt(scanner.nextLine());
    }

    public String solicitarCriptomoeda() {
        System.out.print(MessageProvider.get("transaction.crypto") + " ");
        return scanner.nextLine().toUpperCase();
    }

    public double solicitarQuantidade() {
        System.out.print(MessageProvider.get("transaction.amount") + " ");
        return Double.parseDouble(scanner.nextLine());
    }

    public double solicitarCotacao() {
        System.out.print(MessageProvider.get("transaction.price") + " ");
        return Double.parseDouble(scanner.nextLine());
    }

    public void listarMovimentacoes(List<Movimentacao> movimentacoes) {
        System.out.println("\n--- Extrato de Movimentações ---");
        if (movimentacoes.isEmpty()) {
            System.out.println("Nenhuma movimentação encontrada para esta carteira.");
        } else {
            for (Movimentacao m : movimentacoes) {
                System.out.println(m.toString());
            }
        }
    }

    public void exibirSucesso() {
        System.out.println(MessageProvider.get("msg.success"));
    }

    public void exibirMensagemInvalida() {
        System.out.println(MessageProvider.get("msg.invalid"));
    }
}
