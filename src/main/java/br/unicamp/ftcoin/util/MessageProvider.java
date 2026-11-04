package br.unicamp.ftcoin.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Textos e formatacao dependentes do idioma.
 *
 * <p>E um Singleton porque carrega estado com ciclo de vida — o idioma corrente,
 * o {@link ResourceBundle} e os tres formatadores — e precisa existir uma unica
 * vez: o idioma e escolhido na partida e lido por todas as telas. Um conjunto de
 * membros estaticos ja seria um Singleton disfarcado; o padrao apenas torna a
 * instancia unica e seu ciclo de vida explicitos.</p>
 *
 * <p>A moeda fica fixada em BRL em qualquer idioma: a FT-Coin e cotada em reais
 * independentemente de quem le a tela. O que muda com o idioma e a posicao do
 * simbolo e os separadores decimal e de milhar.</p>
 */
public final class MessageProvider {

    private static final String BASE_NAME = "message";
    private static final Currency COIN_CURRENCY = Currency.getInstance("BRL");
    private static final int MONEY_SCALE = 2;
    private static final int QUANTITY_SCALE = 3;

    private static MessageProvider instance;

    private Locale locale;
    private ResourceBundle bundle;
    private NumberFormat moneyFormat;
    private NumberFormat quantityFormat;
    private DateTimeFormatter dateFormat;

    private MessageProvider() {
        setLocale(Locale.forLanguageTag("pt-BR"));
    }

    public static synchronized MessageProvider getInstance() {
        if (instance == null) {
            instance = new MessageProvider();
        }
        return instance;
    }

    /** Troca o idioma e reconstroi bundle e formatadores. */
    public void setLocale(Locale newLocale) {
        this.locale = newLocale;
        this.bundle = ResourceBundle.getBundle(BASE_NAME, newLocale);

        this.moneyFormat = NumberFormat.getCurrencyInstance(newLocale);
        this.moneyFormat.setCurrency(COIN_CURRENCY);

        this.quantityFormat = NumberFormat.getNumberInstance(newLocale);
        this.quantityFormat.setMinimumFractionDigits(QUANTITY_SCALE);
        this.quantityFormat.setMaximumFractionDigits(QUANTITY_SCALE);

        this.dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(newLocale);
    }

    public Locale locale() {
        return locale;
    }

    /** Texto da chave, ou {@code !chave!} quando ela nao existe no bundle. */
    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    /** Texto da chave com os argumentos aplicados, no idioma corrente. */
    public String format(String key, Object... arguments) {
        return String.format(locale, get(key), arguments);
    }

    /** Unico ponto do sistema que arredonda dinheiro para exibicao. */
    public String money(BigDecimal value) {
        return moneyFormat.format(value.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
    }

    /** Quantidade de moeda com 3 casas, espelhando MOVIMENTACAO.Quantidade. */
    public String quantity(BigDecimal value) {
        return quantityFormat.format(value);
    }

    public String date(LocalDate value) {
        return value.format(dateFormat);
    }
}
