package br.unicamp.ftcoin.view;

import br.unicamp.ftcoin.dao.PersistenceType;
import br.unicamp.ftcoin.oracle.QuoteSourceType;
import br.unicamp.ftcoin.util.Console;
import br.unicamp.ftcoin.util.MessageProvider;

import java.util.Locale;

/**
 * Perguntas feitas antes do menu principal: idioma e modo de persistencia.
 *
 * <p>Fica na camada de apresentacao, e nao na classe de partida, para que
 * {@code Start} apenas monte a aplicacao e nao converse com o usuario.</p>
 */
public class StartupView extends BaseView {

    private static final Locale PORTUGUESE = Locale.forLanguageTag("pt-BR");
    private static final Locale ENGLISH = Locale.forLanguageTag("en-US");
    private static final Locale SPANISH = Locale.forLanguageTag("es-ES");

    /**
     * Pergunta o idioma antes de qualquer outra coisa.
     *
     * <p>Os textos vem do bundle padrao: esta e a unica tela que roda antes de o
     * idioma estar escolhido, por isso a pergunta aparece nos tres idiomas.</p>
     */
    public Locale askLanguage() {
        MessageProvider provider = MessageProvider.getInstance();
        System.out.println(provider.get("startup.selectLanguage"));
        System.out.println(provider.get("startup.language.pt"));
        System.out.println(provider.get("startup.language.en"));
        System.out.println(provider.get("startup.language.es"));
        return switch (readChoice()) {
            case "2" -> ENGLISH;
            case "3" -> SPANISH;
            default -> PORTUGUESE;
        };
    }

    public PersistenceType askPersistence() {
        System.out.println();
        System.out.println(messages.get("startup.selectPersistence"));
        System.out.println(messages.get("startup.persistence.memory"));
        System.out.println(messages.get("startup.persistence.relational"));
        return "2".equals(readChoice()) ? PersistenceType.RELATIONAL : PersistenceType.MEMORY;
    }

    /** Terceira pergunta da partida: de onde vem a cotacao. */
    public QuoteSourceType askQuoteSource() {
        System.out.println();
        System.out.println(messages.get("startup.selectQuoteSource"));
        System.out.println(messages.get("startup.quoteSource.formula"));
        System.out.println(messages.get("startup.quoteSource.api"));
        return "2".equals(readChoice()) ? QuoteSourceType.API : QuoteSourceType.FORMULA;
    }

    /** Informa qual servidor de cotacoes sera usado. Nunca mostra a senha. */
    public void showQuoteServerInUse(String address) {
        Console.warn(messages.format("startup.quoteSource.usingApi", address));
    }

    /** Informa qual banco sera usado, no idioma escolhido. */
    public void showDatabaseInUse(String url) {
        Console.warn(messages.format("startup.persistence.usingMariaDb", url));
    }

    public void showFatalError(String messageKey) {
        Console.error(messages.get(messageKey));
    }
}
