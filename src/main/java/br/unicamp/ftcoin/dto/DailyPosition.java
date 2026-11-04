package br.unicamp.ftcoin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Posicao da carteira em um dia com movimentacao.
 *
 * <p>Os campos correspondem, um a um, as colunas da planilha de referencia
 * {@code Docs/FTCoinApuracaoResultado.ods}, de modo que o relatorio de historico
 * possa ser conferido linha a linha contra ela.</p>
 *
 * @param dayIndex          indice do dia na serie do oraculo
 * @param date              data das operacoes agregadas nesta linha
 * @param bought            quantidade comprada no dia
 * @param sold              quantidade vendida no dia
 * @param balance           saldo em moeda apos o dia
 * @param price             cotacao do dia
 * @param averageCost       custo medio ponderado apos o dia
 * @param purchaseValue     financeiro comprado no dia
 * @param saleValue         financeiro vendido no dia
 * @param operationResult   resultado realizado na venda do dia
 * @param accumulatedResult soma dos resultados ate o dia
 * @param costBalance       saldo valorado ao custo medio
 * @param marketBalance     saldo valorado a cotacao do dia
 */
public record DailyPosition(
        int dayIndex,
        LocalDate date,
        BigDecimal bought,
        BigDecimal sold,
        BigDecimal balance,
        BigDecimal price,
        BigDecimal averageCost,
        BigDecimal purchaseValue,
        BigDecimal saleValue,
        BigDecimal operationResult,
        BigDecimal accumulatedResult,
        BigDecimal costBalance,
        BigDecimal marketBalance) {
}
