package br.unicamp.ftcoin.dao.memory;

import br.unicamp.ftcoin.dao.interfaces.IMovimentacaoDAO;
import br.unicamp.ftcoin.dto.Movimentacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação em memória do DAO de Movimentacao.
 */
public class MovimentacaoMemoryDAO implements IMovimentacaoDAO {

    private final Map<Integer, Movimentacao> banco = new HashMap<>();
    private int sequencia = 0;

    @Override
    public boolean inserir(Movimentacao movimentacao) {
        if (movimentacao == null || banco.containsKey(movimentacao.getIdMovimento())) {
            return false;
        }
        banco.put(movimentacao.getIdMovimento(), movimentacao);
        if (movimentacao.getIdMovimento() > sequencia) {
            sequencia = movimentacao.getIdMovimento();
        }
        return true;
    }

    @Override
    public Movimentacao consultar(int idMovimento) {
        return banco.get(idMovimento);
    }

    @Override
    public List<Movimentacao> listarTodas() {
        return new ArrayList<>(banco.values());
    }

    @Override
    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        List<Movimentacao> resultado = new ArrayList<>();
        for (Movimentacao m : banco.values()) {
            if (m.getIdCarteira() == idCarteira) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    @Override
    public boolean excluirPorCarteira(int idCarteira) {
        List<Integer> aRemover = new ArrayList<>();
        for (Movimentacao m : banco.values()) {
            if (m.getIdCarteira() == idCarteira) {
                aRemover.add(m.getIdMovimento());
            }
        }
        for (Integer id : aRemover) {
            banco.remove(id);
        }
        return !aRemover.isEmpty();
    }

    @Override
    public int proximoId() {
        return ++sequencia;
    }
}
