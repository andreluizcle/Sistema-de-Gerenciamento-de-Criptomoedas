package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.MessageProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Classe base das Views. Centraliza a leitura do teclado e oferece
 * utilitarios comuns (impressao de cabecalho, pausa, leitura de inteiro,
 * leitura de decimal e leitura de data).
 *
 * Demonstra heranca: as Views especificas estendem esta classe.
 */
public abstract class ViewBase {

    /** Scanner unico compartilhado por todas as Views e tambem pela classe Start. */
    public static final Scanner scanner = new Scanner(System.in);

    protected void cabecalho(String chaveTitulo) {
        Console.titulo(MessageProvider.get(chaveTitulo));
    }

    protected void pausar() {
        System.out.print(MessageProvider.get("geral.pressione.enter"));
        scanner.nextLine();
    }

    protected void mostrarOpcaoInvalida() {
        Console.erro(MessageProvider.get("geral.opcao.invalida"));
    }

    /** Le uma linha de texto exibindo o prompt informado. */
    protected String lerLinha(String chavePrompt) {
        System.out.print(MessageProvider.get(chavePrompt) + " ");
        return scanner.nextLine().trim();
    }

    /** Le um inteiro, retornando Integer.MIN_VALUE em caso de erro de formato. */
    protected int lerInteiro(String chavePrompt) {
        String entrada = lerLinha(chavePrompt);
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    /** Le um BigDecimal positivo. Retorna null se invalido. */
    protected BigDecimal lerDecimal(String chavePrompt) {
        String entrada = lerLinha(chavePrompt).replace(",", ".");
        try {
            return new BigDecimal(entrada);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Le uma data no formato AAAA-MM-DD. Se a entrada for vazia,
     * retorna a data de hoje. Retorna null em caso de formato invalido.
     */
    protected LocalDate lerData(String chavePrompt) {
        String entrada = lerLinha(chavePrompt);
        if (entrada.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(entrada);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Retorna true se o usuario confirmar com 'S' (pt) ou 'Y' (en). */
    protected boolean confirmar(String mensagem) {
        System.out.print(mensagem + " " + MessageProvider.get("geral.confirmar") + " ");
        String resposta = scanner.nextLine().trim().toUpperCase();
        String sim = MessageProvider.get("geral.sim").toUpperCase();
        return resposta.equals(sim);
    }
}
