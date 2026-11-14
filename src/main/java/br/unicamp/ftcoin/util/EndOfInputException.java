package br.unicamp.ftcoin.util;

/**
 * A entrada padrao terminou enquanto uma tela esperava uma linha.
 *
 * <p>Acontece com {@code Ctrl-D} em qualquer prompt e quando o programa roda com
 * a entrada vinda de arquivo ou de cano e o roteiro acaba antes de o usuario
 * escolher sair.</p>
 *
 * <p>Deliberadamente <b>nao</b> estende {@link FtCoinException}, e a diferenca e
 * o ponto da classe. Os lacos dos controladores capturam {@code FtCoinException}
 * e voltam ao menu; se esta falha fosse capturada ali, o menu pediria entrada de
 * novo, tornaria a receber fim de arquivo e o programa giraria para sempre. Por
 * nao ser traduzivel ela atravessa os lacos e e tratada em um unico lugar,
 * {@code Start.main}, que encerra a sessao como se o usuario tivesse escolhido
 * sair.</p>
 */
public class EndOfInputException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EndOfInputException() {
        super("end of input");
    }
}
