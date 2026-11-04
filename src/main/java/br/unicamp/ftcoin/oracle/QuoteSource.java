package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.dto.Quote;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Cotacoes do intervalo, inclusive nas duas pontas, em uma unica consulta.
     *
     * <p>A implementacao padrao percorre as datas uma a uma, o que serve as
     * fontes locais, onde nao ha ida a rede a economizar. Fontes de rede
     * sobrescrevem este metodo para trazer o intervalo inteiro em um pedido
     * so.</p>
     *
     * <p>E uma consulta de melhor esforco: pode devolver menos datas do que o
     * pedido, ou nenhuma, sem que isso seja erro. Quem precisa de uma data
     * especifica usa {@link #fetch(LocalDate)}, que falha quando nao ha
     * cotacao.</p>
     */
    default List<Quote> fetchBetween(LocalDate from, LocalDate to) {
        List<Quote> quotes = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            quotes.add(fetch(date));
        }
        return quotes;
    }
}
