package br.unicamp.ftcoin.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO que representa uma entrada do Oráculo, contendo a cotação
 * diária da moeda virtual em moeda real.
 */
public class Oraculo implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate data;
    private BigDecimal cotacao;

    public Oraculo() {
    }

    public Oraculo(LocalDate data, BigDecimal cotacao) {
        this.data = data;
        this.cotacao = cotacao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getCotacao() {
        return cotacao;
    }

    public void setCotacao(BigDecimal cotacao) {
        this.cotacao = cotacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Oraculo oraculo = (Oraculo) o;
        return Objects.equals(data, oraculo.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return String.format("Oraculo [Data=%s, Cotacao=%s]", data, cotacao);
    }
}
