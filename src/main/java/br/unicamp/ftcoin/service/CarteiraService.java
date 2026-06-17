package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dao.interfaces.ICarteiraDAO;
import br.unicamp.ftcoin.dao.interfaces.IMovimentacaoDAO;
import br.unicamp.ftcoin.dto.Carteira;

import java.util.Comparator;
import java.util.List;

/**
 * Camada de servico para Carteira.
 * Encapsula as regras de negocio (CRUD, validacoes e ordenacao)
 * e abstrai a camada de persistencia (DAO) dos controllers.
 */
public class CarteiraService {

    private final ICarteiraDAO carteiraDAO;
    private final IMovimentacaoDAO movimentacaoDAO;

    public CarteiraService(ICarteiraDAO carteiraDAO, IMovimentacaoDAO movimentacaoDAO) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
    }

    public boolean incluir(Carteira carteira) {
        if (!validar(carteira)) {
            return false;
        }
        return carteiraDAO.inserir(carteira);
    }

    public Carteira consultar(int id) {
        return carteiraDAO.consultar(id);
    }

    public boolean atualizar(Carteira carteira) {
        if (!validar(carteira)) {
            return false;
        }
        return carteiraDAO.atualizar(carteira);
    }

    /**
     * Exclui a carteira e todas as movimentacoes relacionadas.
     * Garante a integridade referencial mesmo no modo memoria.
     */
    public boolean excluir(int id) {
        if (!carteiraDAO.existe(id)) {
            return false;
        }
        movimentacaoDAO.excluirPorCarteira(id);
        return carteiraDAO.excluir(id);
    }

    public boolean existe(int id) {
        return carteiraDAO.existe(id);
    }

    public List<Carteira> listarOrdenadasPorId() {
        List<Carteira> lista = carteiraDAO.listarTodas();
        lista.sort(Comparator.comparingInt(Carteira::getId));
        return lista;
    }

    public List<Carteira> listarOrdenadasPorTitular() {
        List<Carteira> lista = carteiraDAO.listarTodas();
        lista.sort(Comparator.comparing(
                c -> c.getNomeTitular() == null ? "" : c.getNomeTitular().toLowerCase()));
        return lista;
    }

    public List<Carteira> listarTodas() {
        return carteiraDAO.listarTodas();
    }

    private boolean validar(Carteira carteira) {
        return carteira != null
                && carteira.getId() > 0
                && carteira.getNomeTitular() != null && !carteira.getNomeTitular().trim().isEmpty()
                && carteira.getCorretora() != null && !carteira.getCorretora().trim().isEmpty();
    }
}
