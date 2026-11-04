package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.util.FtCoinException;

/** O oraculo nao tem cotacao para a data pedida. */
public class QuoteUnavailableException extends FtCoinException {

    private static final long serialVersionUID = 1L;

    public QuoteUnavailableException(String messageKey) {
        super(messageKey);
    }

    /**
     * @param cause falha original — o texto devolvido pelo servidor ou a
     *              excecao de rede. Nao e mostrada ao usuario, que ve a
     *              mensagem traduzida, mas e gravada no registro de execucao.
     */
    public QuoteUnavailableException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }
}
