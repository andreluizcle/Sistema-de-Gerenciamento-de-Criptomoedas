package br.unicamp.ftcoin.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO que representa uma Carteira de moeda virtual.
 * Responsável por encapsular os dados de identificação da carteira,
 * do titular e da corretora utilizada.
 */
public class Carteira implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nomeTitular;
    private String corretora;

    public Carteira() {
    }

    public Carteira(int id, String nomeTitular, String corretora) {
        this.id = id;
        this.nomeTitular = nomeTitular;
        this.corretora = corretora;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeTitular() {
        return nomeTitular;
    }

    public void setNomeTitular(String nomeTitular) {
        this.nomeTitular = nomeTitular;
    }

    public String getCorretora() {
        return corretora;
    }

    public void setCorretora(String corretora) {
        this.corretora = corretora;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Carteira carteira = (Carteira) o;
        return id == carteira.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Carteira [ID=%d, Titular=%s, Corretora=%s]",
                id, nomeTitular, corretora);
    }
}
