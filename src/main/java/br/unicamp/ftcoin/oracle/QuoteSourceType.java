package br.unicamp.ftcoin.oracle;

/**
 * Origens de cotacao oferecidas na partida do programa.
 *
 * <p>O enum e o lugar certo para a fabrica: o conjunto de opcoes de menu e
 * fechado, enquanto {@link QuoteSource} continua aberto a implementacoes que
 * nao aparecem aqui. E o equivalente de {@code DAOFactory.of} para o
 * oraculo.</p>
 */
public enum QuoteSourceType {

    /** Formula local do projeto: deterministica e independente de rede. */
    FORMULA,

    /** Servidor de cotacoes da faculdade, pela API de objetos sobre socket. */
    API;

    /** Monta a fonte correspondente. Nao abre conexao. */
    public QuoteSource create() {
        return switch (this) {
            case FORMULA -> new FormulaQuoteSource();
            case API -> ApiQuoteSource.fromEnvironment();
        };
    }
}
