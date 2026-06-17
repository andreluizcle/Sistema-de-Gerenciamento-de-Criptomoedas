package br.unicamp.ftcoin.dao.interfaces;

import br.unicamp.ftcoin.dto.Carteira;

import java.util.List;

/**
 * Contrato para operações de persistência da entidade Carteira.
 * Permite que a aplicação utilize tanto a implementação em memória
 * quanto a implementação relacional (MariaDB) de forma transparente.
 */
public interface ICarteiraDAO {

    /** Insere uma nova carteira. */
    boolean inserir(Carteira carteira);

    /** Consulta uma carteira pelo identificador. */
    Carteira consultar(int id);

    /** Atualiza os dados de uma carteira existente. */
    boolean atualizar(Carteira carteira);

    /** Exclui uma carteira pelo identificador. */
    boolean excluir(int id);

    /** Lista todas as carteiras cadastradas. */
    List<Carteira> listarTodas();

    /** Verifica se já existe uma carteira com o identificador informado. */
    boolean existe(int id);
}
