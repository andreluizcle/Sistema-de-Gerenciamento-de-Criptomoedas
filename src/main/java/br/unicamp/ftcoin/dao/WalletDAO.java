package br.unicamp.ftcoin.dao;

import br.unicamp.ftcoin.dto.Wallet;

import java.util.List;

/**
 * Contrato de persistencia de carteiras.
 *
 * <p>Selada: as duas implementacoes previstas pelo projeto sao as unicas
 * possiveis, e e o compilador que garante isso, nao a disciplina da equipe.
 * Nao ha razao para uma terceira persistencia aparecer sem revisao do
 * contrato.</p>
 *
 * <p>Os subtipos permitidos moram neste mesmo pacote porque {@code permits}
 * exige mesmo pacote fora de um modulo nomeado, e o projeto e entregue como
 * arquivos {@code .java} compilados com classpath.</p>
 */
public sealed interface WalletDAO permits MemoryWalletDAO, RelationalWalletDAO {

    /** Insere a carteira e atribui a ela o identificador gerado. */
    boolean insert(Wallet wallet);

    /** Carteira do identificador, ou {@code null} se nao existir. */
    Wallet findById(int id);

    boolean update(Wallet wallet);

    boolean delete(int id);

    List<Wallet> findAll();
}
