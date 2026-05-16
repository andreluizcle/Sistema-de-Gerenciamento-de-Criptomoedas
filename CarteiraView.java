package java.view;

import java.MessageProvider;
import java.dto.Carteira;
import java.util.List;
import java.util.Scanner;

public class CarteiraView {
    private Scanner scanner;

    public CarteiraView() {
        this.scanner = new Scanner(System.in);
    }

    public int exibirMenu() {
        System.out.println("\n" + MessageProvider.get("wallet.title"));
        System.out.println(MessageProvider.get("wallet.add"));
        System.out.println(MessageProvider.get("wallet.list"));
        System.out.println("0. Voltar");
        System.out.print(MessageProvider.get("menu.option") + " ");
        
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String solicitarNomeCarteira() {
        System.out.print(MessageProvider.get("wallet.name") + " ");
        return scanner.nextLine();
    }

    public void listarCarteiras(List<Carteira> carteiras) {
        System.out.println("\n--- Lista de Carteiras ---");
        if (carteiras.isEmpty()) {
            System.out.println("Nenhuma carteira cadastrada.");
        } else {
            for (Carteira c : carteiras) {
                System.out.println(c.toString());
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
