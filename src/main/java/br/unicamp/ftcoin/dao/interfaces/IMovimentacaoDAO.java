package br.unicamp.ftcoin.dao.interfaces;

import br.unicamp.ftcoin.dto.Movimentacao;

import java.util.List;

/**
 * Contrato para operações de persistência da entidade Movimentacao.
 */
public interface IMovimentacaoDAO {

    /** Insere uma nova movimentação. */
    boolean inserir(Movimentacao movimentacao);

    /** Consulta uma movimentação pelo identificador do movimento. */
    Movimentacao consultar(int idMovimento);

    /** Lista todas as movimentações cadastradas. */
    List<Movimentacao> listarTodas();

    /** Lista as movimentações de uma carteira específica. */
    List<Movimentacao> listarPorCarteira(int idCarteira);

    /** Exclui todas as movimentações de uma carteira (uso interno). */
    boolean excluirPorCarteira(int idCarteira);

    /** Gera próximo identificador disponível para nova movimentação. */
    int proximoId();
}
