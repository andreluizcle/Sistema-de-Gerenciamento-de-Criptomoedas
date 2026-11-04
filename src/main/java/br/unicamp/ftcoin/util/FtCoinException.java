package br.unicamp.ftcoin.util;

/**
 * Falha da aplicacao que tem uma mensagem traduzivel para o usuario.
 *
 * <p>Carrega a chave do bundle, e nao o texto pronto, para que a camada de
 * apresentacao decida o idioma. A causa original — por exemplo uma
 * {@code SQLException} — fica disponivel para depuracao mas nunca e mostrada ao
 * usuario, ja que mensagens de driver nao sao traduzidas.</p>
 */
public abstract class FtCoinException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String messageKey;

    protected FtCoinException(String messageKey, Throwable cause) {
        super(messageKey, cause);
        this.messageKey = messageKey;
    }

    protected FtCoinException(String messageKey) {
        this(messageKey, null);
    }

    /** Chave do bundle correspondente a esta falha. */
    public String getMessageKey() {
        return messageKey;
    }
}
