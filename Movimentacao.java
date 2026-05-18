package dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Movimentacao implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Tipo {
        COMPRA('C'), VENDA('V');
        
        private final char sigla;
        Tipo(char sigla) { this.sigla = sigla; }
        public char getSigla() { return sigla; }
        
        public static Tipo fromChar(char sigla) {
            if (sigla == 'C' || sigla == 'c') return COMPRA;
            if (sigla == 'V' || sigla == 'v') return VENDA;
            throw new IllegalArgumentException("Tipo de operação inválido: " + sigla);
        }
    }
    private int id;
    private int carteiraId;
    private LocalDate dataOperacao;
    private Tipo tipo;
    private BigDecimal quantidade;

    public Movimentacao() {}

    public Movimentacao(int id, int carteiraId, LocalDate dataOperacao, Tipo tipo, BigDecimal quantidade) {
        this.id = id;
        this.carteiraId = carteiraId;
        this.dataOperacao = (dataOperacao != null) ? dataOperacao : LocalDate.now();
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCarteiraId() { return carteiraId; }
    public void setCarteiraId(int carteiraId) { this.carteiraId = carteiraId; }
    public LocalDate getDataOperacao() { return dataOperacao; }
    public void setDataOperacao(LocalDate dataOperacao) { this.dataOperacao = dataOperacao; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    @Override
    public String toString() {
        return String.format("[%s] %s | Carteira ID: %d | Qtd: %s", 
                dataOperacao.toString(), tipo, carteiraId, quantidade.toString());
    }
}
