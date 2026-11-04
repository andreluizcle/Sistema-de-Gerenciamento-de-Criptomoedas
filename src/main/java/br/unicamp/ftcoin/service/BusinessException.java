package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.util.FtCoinException;

/**
 * Regra de negocio violada — saldo insuficiente, quantidade invalida, dados
 * obrigatorios ausentes.
 *
 * <p>Existe para que o motivo da recusa chegue a tela. A versao anterior
 * devolvia {@code null} tanto para quantidade invalida quanto para saldo
 * insuficiente, e o controlador tinha de adivinhar qual mensagem exibir.</p>
 */
public class BusinessException extends FtCoinException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String messageKey) {
        super(messageKey);
    }
}
