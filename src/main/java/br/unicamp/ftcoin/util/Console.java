package br.unicamp.ftcoin.util;

/**
 * Impressao colorida no terminal usando codigos ANSI.
 *
 * <p>Funciona na maioria dos terminais modernos. Em terminais legados as cores
 * aparecem como caracteres de escape, mas o texto continua legivel.</p>
 */
public final class Console {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";

    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    private Console() {
    }

    /** Envolve o texto com a cor informada e o reset. */
    public static String paint(String color, String text) {
        return color + text + RESET;
    }

    public static void title(String text) {
        System.out.println();
        System.out.println(paint(BOLD + CYAN, "=== " + text + " ==="));
    }

    public static void success(String text) {
        System.out.println(paint(GREEN, "[OK] " + text));
    }

    public static void error(String text) {
        System.out.println(paint(RED, "[X] " + text));
    }

    public static void warn(String text) {
        System.out.println(paint(YELLOW, "[!] " + text));
    }

    public static void info(String text) {
        System.out.println(paint(BLUE, "[i] " + text));
    }

    /** Limpa a tela do terminal (best-effort). */
    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
