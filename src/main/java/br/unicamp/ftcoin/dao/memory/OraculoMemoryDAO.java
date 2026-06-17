package br.unicamp.ftcoin.dao.memory;

import br.unicamp.ftcoin.dao.interfaces.IOraculoDAO;
import br.unicamp.ftcoin.dto.Oraculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Implementação em memória do DAO de Oraculo.
 * Já vem pré-populado com algumas cotações de exemplo para facilitar
 * a demonstração do sistema.
 */
public class OraculoMemoryDAO implements IOraculoDAO {

    private final TreeMap<LocalDate, Oraculo> banco = new TreeMap<>();

    public OraculoMemoryDAO() {
        // Cotações de exemplo (FT-Coin em R$)
        popularExemplos();
    }

    private void popularExemplos() {
        LocalDate hoje = LocalDate.now();
        BigDecimal[] cotacoes = {
                new BigDecimal("100.00"),
                new BigDecimal("105.50"),
                new BigDecimal("110.20"),
                new BigDecimal("108.75"),
                new BigDecimal("115.30"),
                new BigDecimal("120.00"),
                new BigDecimal("118.45")
        };
        for (int i = 0; i < cotacoes.length; i++) {
            LocalDate data = hoje.minusDays(cotacoes.length - 1 - i);
            banco.put(data, new Oraculo(data, cotacoes[i]));
        }
    }

    @Override
    public boolean inserirOuAtualizar(Oraculo oraculo) {
        if (oraculo == null || oraculo.getData() == null) {
            return false;
        }
        banco.put(oraculo.getData(), oraculo);
        return true;
    }

    @Override
    public Oraculo consultarPorData(LocalDate data) {
        return banco.get(data);
    }

    @Override
    public Oraculo consultarMaisRecente() {
        if (banco.isEmpty()) {
            return null;
        }
        return banco.lastEntry().getValue();
    }

    @Override
    public List<Oraculo> listarTodas() {
        List<Oraculo> lista = new ArrayList<>(banco.values());
        lista.sort(Comparator.comparing(Oraculo::getData));
        return lista;
    }
}
