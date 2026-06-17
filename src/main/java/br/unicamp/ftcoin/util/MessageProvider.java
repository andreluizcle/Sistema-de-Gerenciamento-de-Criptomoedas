package br.unicamp.ftcoin.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provedor de mensagens internacionalizadas.
 * Lê os arquivos message_pt_BR.properties / message_en_US.properties
 * presentes em resources e expõe um método estático para obter os textos.
 */
public final class MessageProvider {

    private static final String BASE_NAME = "message";

    private static Locale localeAtual = new Locale("pt", "BR");
    private static ResourceBundle bundle = carregar(localeAtual);

    private MessageProvider() {
    }

    private static ResourceBundle carregar(Locale locale) {
        return ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public static void setLocale(Locale locale) {
        localeAtual = locale;
        bundle = carregar(locale);
    }

    public static Locale getLocale() {
        return localeAtual;
    }

    /** Retorna o texto associado à chave. */
    public static String get(String chave) {
        try {
            return bundle.getString(chave);
        } catch (MissingResourceException e) {
            return "!" + chave + "!";
        }
    }

    /** Retorna texto formatado, fazendo o substitution dos parâmetros via String.format. */
    public static String get(String chave, Object... argumentos) {
        try {
            return String.format(bundle.getString(chave), argumentos);
        } catch (MissingResourceException e) {
            return "!" + chave + "!";
        }
    }
}
