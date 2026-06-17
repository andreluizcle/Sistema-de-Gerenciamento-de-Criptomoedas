package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dao.interfaces.IOraculoDAO;
import br.unicamp.ftcoin.dto.Oraculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Camada de servico do Oraculo.
 * Encapsula a logica de obtencao de cotacoes e conversao entre
 * moeda virtual e moeda real.
 */
public class OraculoService {

    private final IOraculoDAO oraculoDAO;

    public OraculoService(IOraculoDAO oraculoDAO) {
        this.oraculoDAO = oraculoDAO;
    }

    /**
     * Retorna a cotacao da data informada. Caso nao exista, retorna
     * a cotacao mais recente cadastrada (fallback). Retorna null
     * apenas se nao houver nenhuma cotacao no oraculo.
     */
    public Oraculo cotacaoEm(LocalDate data) {
        Oraculo direto = oraculoDAO.consultarPorData(data);
        if (direto != null) {
            return direto;
        }
        return oraculoDAO.consultarMaisRecente();
    }

    public Oraculo cotacaoMaisRecente() {
        return oraculoDAO.consultarMaisRecente();
    }

    public List<Oraculo> listarHistorico() {
        return oraculoDAO.listarTodas();
    }

    public boolean registrarCotacao(LocalDate data, BigDecimal valor) {
        if (data == null || valor == null || valor.signum() < 0) {
            return false;
        }
        return oraculoDAO.inserirOuAtualizar(new Oraculo(data, valor));
    }

    /** Converte quantidade de moeda virtual em moeda real usando a cotacao do dia. */
    public BigDecimal converterParaReal(BigDecimal quantidade, BigDecimal cotacao) {
        if (quantidade == null || cotacao == null) {
            return BigDecimal.ZERO;
        }
        return quantidade.multiply(cotacao);
    }
}
