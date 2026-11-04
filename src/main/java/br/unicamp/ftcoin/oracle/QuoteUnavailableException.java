package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.util.FtCoinException;

/** O oraculo nao tem cotacao para a data pedida. */
public class QuoteUnavailableException extends FtCoinException {

    private static final long serialVersionUID = 1L;

    public QuoteUnavailableException(String messageKey) {
        super(messageKey);
    }
}
