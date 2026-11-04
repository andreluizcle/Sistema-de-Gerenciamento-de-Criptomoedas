package br.unicamp.ftcoin.dto;

import java.math.BigDecimal;

/**
 * Comprovante de uma compra ou venda concluida.
 *
 * <p>Carrega a cotacao efetivamente usada, o que dispensa a tela de movimentacao
 * de consultar o oraculo por conta propria so para exibi-la.</p>
 *
 * @param transaction movimentacao registrada, ja com o identificador atribuido
 * @param quote       cotacao da data da operacao
 * @param newBalance  saldo da carteira apos a operacao
 */
public record TransactionReceipt(
        Transaction transaction,
        Quote quote,
        BigDecimal newBalance) {

    /** Financeiro da operacao: quantidade multiplicada pela cotacao do dia. */
    public BigDecimal amount() {
        return transaction.getQuantity().multiply(quote.price());
    }
}
