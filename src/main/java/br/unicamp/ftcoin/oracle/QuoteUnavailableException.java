package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.util.FtCoinException;

/**
 * Nao foi possivel obter a cotacao pedida.
 *
 * <p>Cobre tanto a ausencia de cotacao para uma data quanto as falhas de
 * comunicacao com o servidor: rede indisponivel, credenciais recusadas e
 * resposta fora do protocolo. O que distingue os casos e a chave de mensagem
 * que a excecao carrega.</p>
 */
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
