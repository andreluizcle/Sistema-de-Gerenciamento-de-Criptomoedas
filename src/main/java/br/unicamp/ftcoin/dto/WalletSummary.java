package br.unicamp.ftcoin.dto;

import java.math.BigDecimal;

/**
 * Saldo de uma carteira ja valorado, pronto para exibicao.
 *
 * <p>Existe para que as telas de carteira e de relatorio recebam um unico objeto
 * em vez de quatro parametros soltos do mesmo tipo.</p>
 *
 * @param wallet      carteira consultada
 * @param balance     saldo em moeda virtual
 * @param averageCost custo medio ponderado
 * @param costValue   saldo valorado ao custo medio
 * @param marketValue saldo valorado a cotacao corrente
 * @param quote       cotacao usada na valoracao a mercado
 */
public record WalletSummary(
        Wallet wallet,
        BigDecimal balance,
        BigDecimal averageCost,
        BigDecimal costValue,
        BigDecimal marketValue,
        Quote quote) {
}
