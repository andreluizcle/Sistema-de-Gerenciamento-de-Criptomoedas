package br.unicamp.ftcoin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Compra ou venda de moeda virtual em uma carteira.
 *
 * <p>Mutavel pelo mesmo motivo de {@link Wallet}: o identificador e atribuido
 * pelo banco apos o INSERT.</p>
 *
 * <p>A movimentacao guarda apenas a quantidade. O preco nao e armazenado: ele e
 * sempre a cotacao da data da operacao, obtida no oraculo, de modo que nao ha
 * como o historico divergir da fonte de cotacao.</p>
 */
public class Transaction {

    private int id;
    private int walletId;
    private LocalDate date;
    private OperationType type;
    private BigDecimal quantity;

    public Transaction() {
    }

    public Transaction(int id, int walletId, LocalDate date, OperationType type, BigDecimal quantity) {
        this.id = id;
        this.walletId = walletId;
        this.date = date;
        this.type = type;
        this.quantity = quantity;
    }

    /** Copia defensiva, usada pelo DAO em memoria para nao expor seu estado. */
    public Transaction copy() {
        return new Transaction(id, walletId, date, type, quantity);
    }

    public boolean isBuy() {
        return type == OperationType.BUY;
    }

    public boolean isSell() {
        return type == OperationType.SELL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Transaction transaction)) {
            return false;
        }
        return id == transaction.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Representacao tecnica, para depuracao e falhas de teste. Telas nao a usam. */
    @Override
    public String toString() {
        return "Transaction[id=" + id + ", wallet=" + walletId + ", date=" + date
                + ", type=" + type + ", quantity=" + quantity + "]";
    }
}
