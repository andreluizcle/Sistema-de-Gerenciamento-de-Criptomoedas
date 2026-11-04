package br.unicamp.ftcoin.dto;

/**
 * Tipo de uma movimentacao. O codigo de uma letra e o que vai para a coluna
 * MOVIMENTACAO.TipoOperacao, definida como CHAR(1) no enunciado.
 */
public enum OperationType {

    BUY('C'),
    SELL('V');

    private final char code;

    OperationType(char code) {
        this.code = code;
    }

    public char code() {
        return code;
    }

    /** Converte o codigo lido do banco. */
    public static OperationType fromCode(char code) {
        for (OperationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown operation code: " + code);
    }
}
