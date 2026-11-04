package br.unicamp.ftcoin.dto;

import java.util.Objects;

/**
 * Carteira de moeda virtual.
 *
 * <p>E uma classe mutavel, e nao um record, por duas razoes concretas: o
 * identificador so existe depois do INSERT (vem do AUTO_INCREMENT) e a edicao
 * altera titular e corretora de uma carteira ja existente.</p>
 */
public class Wallet {

    private int id;
    private String holderName;
    private String brokerage;

    public Wallet() {
    }

    public Wallet(int id, String holderName, String brokerage) {
        this.id = id;
        this.holderName = holderName;
        this.brokerage = brokerage;
    }

    /** Copia defensiva, usada pelo DAO em memoria para nao expor seu estado. */
    public Wallet copy() {
        return new Wallet(id, holderName, brokerage);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getBrokerage() {
        return brokerage;
    }

    public void setBrokerage(String brokerage) {
        this.brokerage = brokerage;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Wallet wallet)) {
            return false;
        }
        return id == wallet.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Representacao tecnica, para depuracao e falhas de teste. Telas nao a usam. */
    @Override
    public String toString() {
        return "Wallet[id=" + id + ", holder=" + holderName + ", brokerage=" + brokerage + "]";
    }
}
