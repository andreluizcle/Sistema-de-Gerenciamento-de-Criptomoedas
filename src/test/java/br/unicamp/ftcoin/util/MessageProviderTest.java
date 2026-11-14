package br.unicamp.ftcoin.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Internacionalizacao: paridade dos bundles e formatacao por idioma. */
class MessageProviderTest {

    private static final List<String> BUNDLES = List.of(
            "message.properties",
            "message_pt_BR.properties",
            "message_en_US.properties",
            "message_es_ES.properties");

    private static final Locale PORTUGUESE = Locale.forLanguageTag("pt-BR");
    private static final Locale ENGLISH = Locale.forLanguageTag("en-US");
    private static final Locale SPANISH = Locale.forLanguageTag("es-ES");

    private final MessageProvider messages = MessageProvider.getInstance();

    @AfterEach
    void restoreDefaultLocale() {
        messages.setLocale(PORTUGUESE);
    }

    private Properties load(String name) throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(name)) {
            assertNotNull(stream, "bundle ausente: " + name);
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
        return properties;
    }

    @Test
    @DisplayName("Os quatro bundles tem exatamente as mesmas chaves")
    void bundlesHaveTheSameKeys() throws IOException {
        Set<String> reference = new TreeSet<>(load("message_pt_BR.properties").stringPropertyNames());
        for (String bundle : BUNDLES) {
            if (bundle.equals("message_pt_BR.properties")) {
                continue;   // e a propria referencia
            }
            Set<String> keys = new TreeSet<>(load(bundle).stringPropertyNames());
            assertEquals(reference, keys, "chaves divergentes em " + bundle);
        }
    }

    @Test
    @DisplayName("Nenhum bundle traz o simbolo da moeda no texto")
    void bundlesCarryNoCurrencySymbol() throws IOException {
        // O simbolo e a posicao dele sao decididos em tempo de execucao pelo
        // NumberFormat do idioma; deixa-lo no texto quebraria os outros idiomas.
        for (String bundle : BUNDLES) {
            Properties properties = load(bundle);
            for (String key : properties.stringPropertyNames()) {
                assertFalse(properties.getProperty(key).contains("R$"),
                        "simbolo de moeda em " + bundle + " na chave " + key);
            }
        }
    }

    @Test
    @DisplayName("O valor monetario sai em reais nos tres idiomas, com duas casas")
    void moneyKeepsTheCurrencyAcrossLocales() {
        BigDecimal value = new BigDecimal("12287.944");
        for (Locale locale : List.of(PORTUGUESE, ENGLISH, SPANISH)) {
            messages.setLocale(locale);
            String formatted = messages.money(value);
            // Em pt-BR e en-US o real aparece como "R$"; em es-ES o CLDR nao tem
            // simbolo proprio e usa o codigo "BRL". O que importa e que a unidade
            // nao muda com o idioma, so a forma de escreve-la.
            String currencySymbol = Currency.getInstance("BRL").getSymbol(locale);
            assertTrue(formatted.contains(currencySymbol),
                    "a moeda deveria continuar em reais em " + locale + ": " + formatted);
            assertTrue(formatted.contains("94"),
                    "deveria arredondar para 12287,94 em " + locale + ": " + formatted);
        }
    }

    @Test
    @DisplayName("Numeros e datas seguem o idioma escolhido")
    void numbersAndDatesFollowTheLocale() {
        LocalDate date = LocalDate.of(2026, 1, 30);
        BigDecimal value = new BigDecimal("1234.5");

        messages.setLocale(PORTUGUESE);
        String portugueseDate = messages.date(date);
        String portugueseNumber = messages.quantity(value);

        messages.setLocale(ENGLISH);
        String englishDate = messages.date(date);
        String englishNumber = messages.quantity(value);

        assertNotEquals(portugueseDate, englishDate,
                "a data deveria mudar com o idioma: " + portugueseDate + " / " + englishDate);
        assertTrue(portugueseNumber.contains(","),
                "separador decimal portugues: " + portugueseNumber);
        assertTrue(englishNumber.contains("."),
                "separador decimal ingles: " + englishNumber);
    }

    @Test
    @DisplayName("A quantidade sai sempre com tres casas")
    void quantityAlwaysShowsThreeDecimals() {
        messages.setLocale(PORTUGUESE);
        assertEquals("1.000,000", messages.quantity(new BigDecimal("1000")));
    }

    @Test
    @DisplayName("Trocar o idioma troca o texto")
    void switchingLocaleSwitchesTheText() {
        messages.setLocale(PORTUGUESE);
        String portuguese = messages.get("menu.main.1");
        messages.setLocale(SPANISH);
        String spanish = messages.get("menu.main.1");

        assertNotEquals(portuguese, spanish,
                "o menu deveria mudar de idioma: " + portuguese + " / " + spanish);
    }

    @Test
    @DisplayName("Nenhuma chave usada pelos menus falta em nenhum idioma")
    void everyMenuKeyResolvesInEveryLocale() {
        // As telas montam as chaves de menu concatenando o numero da opcao, entao
        // uma chave ausente degradaria silenciosamente para !menu.wallet.4!.
        List<String> keys = List.of(
                "menu.main.1", "menu.main.2", "menu.main.3", "menu.main.4", "menu.main.5",
                "menu.wallet.1", "menu.wallet.2", "menu.wallet.3", "menu.wallet.4", "menu.wallet.0",
                "menu.transaction.1", "menu.transaction.2", "menu.transaction.3", "menu.transaction.0",
                "menu.report.1", "menu.report.2", "menu.report.3", "menu.report.4", "menu.report.5",
                "menu.report.0", "menu.help.1", "menu.help.2", "menu.help.3", "menu.help.0");
        for (Locale locale : List.of(PORTUGUESE, ENGLISH, SPANISH)) {
            messages.setLocale(locale);
            for (String key : keys) {
                assertFalse(messages.get(key).startsWith("!"),
                        "chave ausente em " + locale + ": " + key);
            }
        }
    }

    @Test
    @DisplayName("Chave inexistente nao derruba o programa")
    void missingKeyIsMarkedInsteadOfThrowing() {
        assertEquals("!nao.existe!", messages.get("nao.existe"));
    }
}
