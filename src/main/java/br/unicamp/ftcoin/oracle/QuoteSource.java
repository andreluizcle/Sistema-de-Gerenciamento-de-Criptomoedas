package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.dto.Quote;

import java.time.LocalDate;

/**
 * Origem das cotacoes diarias da moeda virtual.
 *
 * <p>Diferente dos contratos DAO, esta interface <b>nao</b> e selada, e a
 * diferenca e proposital: o conjunto de persistencias e fechado e conhecido
 * (memoria e MariaDB), enquanto o de fontes de cotacao e aberto — hoje a formula
 * local, amanha a API propria do professor, depois possivelmente uma com
 * fallback. Selar aqui impediria exatamente a extensao que se espera.</p>
 */
public interface QuoteSource {

    /**
     * Cotacao da data pedida.
     *
     * @throws QuoteUnavailableException quando a fonte nao cobre a data
     */
    Quote fetch(LocalDate date);
}
