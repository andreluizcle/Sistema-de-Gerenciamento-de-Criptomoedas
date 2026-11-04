package br.unicamp.ftcoin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cotacao da moeda virtual em uma data.
 *
 * <p>Record porque o par data/preco e imutavel por natureza: uma cotacao
 * passada nunca muda.</p>
 *
 * @param date  data a que a cotacao se refere
 * @param price preco em moeda real
 */
public record Quote(LocalDate date, BigDecimal price) {
}
