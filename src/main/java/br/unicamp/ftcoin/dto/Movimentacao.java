package br.unicamp.ftcoin.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO que representa uma Movimentação de compra ou venda de moeda virtual
 * realizada em uma carteira.
 */
public class Movimentacao implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 'C' para compra, 'V' para venda. */
    public static final char COMPRA = 'C';
    public static final char VENDA = 'V';

    private int idMovimento;
    private int idCarteira;
    private LocalDate dataOperacao;
    private char tipoOperacao;
    private BigDecimal quantidade;

    public Movimentacao() {
    }

    public Movimentacao(int idMovimento, int idCarteira, LocalDate dataOperacao,
                        char tipoOperacao, BigDecimal quantidade) {
        this.idMovimento = idMovimento;
        this.idCarteira = idCarteira;
        this.dataOperacao = dataOperacao;
        this.tipoOperacao = tipoOperacao;
        this.quantidade = quantidade;
    }

    public int getIdMovimento() {
        return idMovimento;
    }

    public void setIdMovimento(int idMovimento) {
        this.idMovimento = idMovimento;
    }

    public int getIdCarteira() {
        return idCarteira;
    }

    public void setIdCarteira(int idCarteira) {
        this.idCarteira = idCarteira;
    }

    public LocalDate getDataOperacao() {
        return dataOperacao;
    }

    public void setDataOperacao(LocalDate dataOperacao) {
        this.dataOperacao = dataOperacao;
    }

    public char getTipoOperacao() {
        return tipoOperacao;
    }

    public void setTipoOperacao(char tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public boolean isCompra() {
        return tipoOperacao == COMPRA;
    }

    public boolean isVenda() {
        return tipoOperacao == VENDA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movimentacao that = (Movimentacao) o;
        return idMovimento == that.idMovimento;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMovimento);
    }

    @Override
    public String toString() {
        return String.format("Movimentacao [Mov=%d, Carteira=%d, Data=%s, Tipo=%c, Qtde=%s]",
                idMovimento, idCarteira, dataOperacao, tipoOperacao, quantidade);
    }
}
