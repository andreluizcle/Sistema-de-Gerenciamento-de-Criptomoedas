package br.unicamp.ftcoin.dao.interfaces;

import br.unicamp.ftcoin.dto.Oraculo;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato para operações de persistência da entidade Oraculo (cotação diária).
 */
public interface IOraculoDAO {

    /** Insere ou atualiza a cotação de uma data específica. */
    boolean inserirOuAtualizar(Oraculo oraculo);

    /** Consulta a cotação para uma data específica. */
    Oraculo consultarPorData(LocalDate data);

    /** Consulta a cotação mais recente disponível. */
    Oraculo consultarMaisRecente();

    /** Lista todas as cotações armazenadas. */
    List<Oraculo> listarTodas();
}
