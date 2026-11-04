package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.MessageProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Base das telas: leitura do teclado e utilitarios comuns.
 *
 * <p>O {@link Scanner} e privado e estatico — ha uma unica entrada padrao, e so
 * a camada de apresentacao tem motivo para le-la. Na versao anterior ele era
 * publico e a classe de partida o usava diretamente, furando as camadas.</p>
 */
public abstract class BaseView {

    private static final Scanner SCANNER = new Scanner(System.in);

    /** Textos e formatacao no idioma corrente. */
    protected final MessageProvider messages = MessageProvider.getInstance();

    protected void header(String titleKey) {
        Console.title(messages.get(titleKey));
    }

    protected void pause() {
        System.out.print(messages.get("general.pressEnter"));
        SCANNER.nextLine();
    }

    public void showInvalidOption() {
        Console.error(messages.get("general.invalidOption"));
    }

    public void showSuccess(String key) {
        Console.success(messages.get(key));
    }

    public void showError(String key) {
        Console.error(messages.get(key));
    }

    /**
     * Le uma escolha com o prompt generico "&gt;".
     *
     * <p>Serve as perguntas da partida, feitas antes de o idioma estar definido.
     * Usa o mesmo {@link Scanner} do restante das telas: duas instancias sobre
     * {@code System.in} disputariam o buffer e perderiam linhas.</p>
     */
    protected String readChoice() {
        System.out.print("> ");
        return SCANNER.nextLine().trim();
    }

    protected String readLine(String promptKey) {
        System.out.print(messages.get(promptKey) + " ");
        return SCANNER.nextLine().trim();
    }

    /** Le um inteiro; devolve {@link Integer#MIN_VALUE} se o formato for invalido. */
    protected int readInt(String promptKey) {
        try {
            return Integer.parseInt(readLine(promptKey));
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    /** Le uma quantidade; devolve {@code null} se o formato for invalido. */
    protected BigDecimal readDecimal(String promptKey) {
        String input = readLine(promptKey);
        if (input.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(normalizeSeparators(input));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Aceita tanto "1.234,56" quanto "1,234.56" e "1234.56".
     *
     * <p>Quando ha virgula, ela e o separador decimal e os pontos sao de milhar;
     * sem virgula, o ponto e o separador decimal. Evita obrigar o usuario a
     * saber qual notacao o idioma escolhido espera.</p>
     */
    private String normalizeSeparators(String input) {
        String cleaned = input.replace(" ", "");
        if (cleaned.indexOf(',') >= 0) {
            return cleaned.replace(".", "").replace(',', '.');
        }
        return cleaned;
    }

    /** Le uma data ISO (AAAA-MM-DD); vazio significa hoje, invalido devolve {@code null}. */
    protected LocalDate readDate(String promptKey) {
        String input = readLine(promptKey);
        if (input.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** {@code true} se o usuario confirmar com a letra do idioma corrente. */
    protected boolean confirm(String message) {
        System.out.print(message + " " + messages.get("general.confirm") + " ");
        String answer = SCANNER.nextLine().trim().toUpperCase();
        return answer.equals(messages.get("general.yes").toUpperCase());
    }
}
