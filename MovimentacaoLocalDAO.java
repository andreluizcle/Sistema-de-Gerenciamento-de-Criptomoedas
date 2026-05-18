package java.dao.local;

import java.dao.interface.IMovimentacaoDAO;
import java.dto.Movimentacao;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovimentacaoLocalDAO implements IMovimentacaoDAO {

    private static final String FILE_PATH = "movimentacoes.dat";

    @Override
    public void salvar(Movimentacao movimentacao) {
        List<Movimentacao> movimentacoes = listarTodas();
        
        // Auto-incremento do ID
        int novoId = movimentacoes.stream()
                                  .mapToInt(Movimentacao::getId)
                                  .max()
                                  .orElse(0) + 1;
        movimentacao.setId(novoId);
        
        movimentacoes.add(movimentacao);
        salvarNoArquivo(movimentacoes);
    }

    @Override
    public List<Movimentacao> buscarPorCarteira(int carteiraId) {
        return listarTodas().stream()
                            .filter(m -> m.getCarteiraId() == carteiraId)
                            .collect(Collectors.toList());
    }

    @Override
    public List<Movimentacao> listarTodas() {
        File arquivo = new File(FILE_PATH);
        if (!arquivo.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<Movimentacao>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao ler movimentações locais: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void salvarNoArquivo(List<Movimentacao> movimentacoes) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(movimentacoes);
        } catch (IOException e) {
            System.err.println("Erro ao salvar movimentação localmente: " + e.getMessage());
        }
    }
}
