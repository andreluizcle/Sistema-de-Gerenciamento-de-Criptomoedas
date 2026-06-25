package br.unicamp.ftcoin.dao.memory;

import br.unicamp.ftcoin.dao.interfaces.ICarteiraDAO;
import br.unicamp.ftcoin.dto.Carteira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação em memória do DAO de Carteira.
 * Utiliza um HashMap como simulação do banco de dados,
 * permitindo testar o sistema sem a necessidade de um banco real.
 */
public class CarteiraMemoryDAO implements ICarteiraDAO {

    private final Map<Integer, Carteira> banco = new HashMap<>();
    private int sequenceId = 1;

    @Override
    public boolean inserir(Carteira carteira) {
        if (carteira == null) {
            return false;
        }
        
        // Simula o AUTO_INCREMENT do banco de dados real
        if (carteira.getId() <= 0) {
            carteira.setId(sequenceId++);
        } else if (banco.containsKey(carteira.getId())) {
            return false;
        }
        
        banco.put(carteira.getId(), carteira);
        return true;
    }

    @Override
    public Carteira consultar(int id) {
        return banco.get(id);
    }

    @Override
    public boolean atualizar(Carteira carteira) {
        if (carteira == null || !banco.containsKey(carteira.getId())) {
            return false;
        }
        banco.put(carteira.getId(), carteira);
        return true;
    }

    @Override
    public boolean excluir(int id) {
        return banco.remove(id) != null;
    }

    @Override
    public List<Carteira> listarTodas() {
        return new ArrayList<>(banco.values());
    }

    @Override
    public boolean existe(int id) {
        return banco.containsKey(id);
    }
}
