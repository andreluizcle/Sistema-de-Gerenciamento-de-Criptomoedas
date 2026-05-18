package java.dao.local;

import java.dao.interface.ICarteiraDAO;
import java.dto.Carteira;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CarteiraLocalDAO implements ICarteiraDAO {
    
    private static final String FILE_PATH = "carteiras.dat";

    @Override
    public void salvar(Carteira carteira) {
        List<Carteira> carteiras = listarTodas();
        
        // Auto-incremento do ID baseado na lista atual
        int novoId = carteiras.stream()
                              .mapToInt(Carteira::getId)
                              .max()
                              .orElse(0) + 1;
        carteira.setId(novoId);
        
        carteiras.add(carteira);
        salvarNoArquivo(carteiras);
    }

    @Override
    public List<Carteira> listarTodas() {
        File arquivo = new File(FILE_PATH);
        if (!arquivo.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<Carteira>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao ler carteiras locais: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Carteira buscarPorId(int id) {
        return listarTodas().stream()
                            .filter(c -> c.getId() == id)
                            .findFirst()
                            .orElse(null);
    }

    private void salvarNoArquivo(List<Carteira> carteiras) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(carteiras);
        } catch (IOException e) {
            System.err.println("Erro ao salvar carteira localmente: " + e.getMessage());
        }
    }
}
