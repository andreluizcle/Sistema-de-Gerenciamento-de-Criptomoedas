package br.unicamp.ftcoin.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Apuracao completa de uma carteira: a serie diaria e os totais.
 *
 * @param days              uma linha por data com movimentacao, em ordem cronologica
 * @param balance           saldo final em moeda
 * @param averageCost       custo medio ponderado final
 * @param totalPurchases    financeiro total comprado
 * @param totalSales        financeiro total vendido
 * @param accumulatedResult resultado realizado acumulado
 * @param costBalance       saldo final valorado ao custo medio
 * @param marketBalance     saldo final valorado a mercado
 * @param lastQuote         cotacao usada na valoracao a mercado
 */
public record WalletResult(
        List<DailyPosition> days,
        BigDecimal balance,
        BigDecimal averageCost,
        BigDecimal totalPurchases,
        BigDecimal totalSales,
        BigDecimal accumulatedResult,
        BigDecimal costBalance,
        BigDecimal marketBalance,
        Quote lastQuote) {

    public WalletResult {
        days = List.copyOf(days);
    }
}
