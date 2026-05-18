package dto;

import java.io.Serializable;

public class Carteira implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nomeTitular;
    private String corretora;

    public Carteira() {}

    public Carteira(int id, String nomeTitular, String corretora) {
        this.id = id;
        this.nomeTitular = nomeTitular;
        this.corretora = corretora;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }
    public String getCorretora() { return corretora; }
    public void setCorretora(String corretora) { this.corretora = corretora; }
    @Override
    public String toString() {
        return "Carteira [ID:" + id + ", Titular:" + nomeTitular + ", Corretora:" + corretora + "]";
    }
}
