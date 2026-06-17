package br.unicamp.ftcoin.util;

/**
 * Utilitário para impressão colorida no terminal usando códigos ANSI.
 * Funciona na maioria dos terminais modernos (Linux, macOS, e Windows 10+ com
 * Terminal/PowerShell modernos). Em terminais legados, as cores aparecerão
 * como caracteres de escape, mas a saída textual permanece legível.
 */
public final class Console {

    public static final String RESET = "\u001B[0m";
    public static final String NEGRITO = "\u001B[1m";

    // Cores de texto
    public static final String PRETO = "\u001B[30m";
    public static final String VERMELHO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String AMARELO = "\u001B[33m";
    public static final String AZUL = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CIANO = "\u001B[36m";
    public static final String BRANCO = "\u001B[37m";

    private Console() {
    }

    /** Envolve o texto com a cor informada e o reset. */
    public static String colorir(String cor, String texto) {
        return cor + texto + RESET;
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println(colorir(NEGRITO + CIANO, "=== " + texto + " ==="));
    }

    public static void sucesso(String texto) {
        System.out.println(colorir(VERDE, "[OK] " + texto));
    }

    public static void erro(String texto) {
        System.out.println(colorir(VERMELHO, "[X] " + texto));
    }

    public static void aviso(String texto) {
        System.out.println(colorir(AMARELO, "[!] " + texto));
    }

    public static void info(String texto) {
        System.out.println(colorir(AZUL, "[i] " + texto));
    }

    /** Limpa a tela do terminal (best-effort). */
    public static void limpar() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
