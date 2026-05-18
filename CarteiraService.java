package service;

import dao.interface.ICarteiraDAO;
import dto.Carteira;

import java.util.List;

public class CarteiraService {

    private final ICarteiraDAO carteiraDAO;

    // Injeção de dependência via construtor — o Service não instancia o DAO,
    // apenas depende da sua interface. Isso permite trocar a implementação
    // (RelationalDAO, mock para testes, etc.) sem alterar esta classe.
    public CarteiraService(ICarteiraDAO carteiraDAO) {
        this.carteiraDAO = carteiraDAO;
    }

    /**
     * Cria uma nova carteira após validar os dados de entrada.
     *
     * @param nomeTitular Nome do titular da carteira
     * @param corretora   Nome da corretora associada
     * @return A carteira criada
     * @throws IllegalArgumentException se os dados forem inválidos
     */
    public Carteira criarCarteira(String nomeTitular, String corretora) {
        validarNomeTitular(nomeTitular);
        validarCorretora(corretora);

        Carteira carteira = new Carteira();
        carteira.setNomeTitular(nomeTitular.trim());
        carteira.setCorretora(corretora.trim());

        return carteiraDAO.salvar(carteira);
    }

    /**
     * Busca uma carteira pelo seu ID.
     *
     * @param id Identificador da carteira
     * @return A carteira encontrada
     * @throws IllegalArgumentException se o ID for inválido
     * @throws RuntimeException         se a carteira não for encontrada
     */
    public Carteira buscarPorId(int id) {
        validarId(id);

        Carteira carteira = carteiraDAO.buscarPorId(id);
        if (carteira == null) {
            throw new RuntimeException("Carteira de ID " + id + " não encontrada.");
        }

        return carteira;
    }

    /**
     * Retorna todas as carteiras cadastradas.
     *
     * @return Lista de carteiras (pode ser vazia, nunca null)
     */
    public List<Carteira> listarTodas() {
        return carteiraDAO.listarTodas();
    }

    /**
     * Atualiza os dados de uma carteira existente.
     *
     * @param id          ID da carteira a ser atualizada
     * @param nomeTitular Novo nome do titular
     * @param corretora   Nova corretora
     * @return A carteira atualizada
     * @throws IllegalArgumentException se os dados forem inválidos
     * @throws RuntimeException         se a carteira não for encontrada
     */
    public Carteira atualizarCarteira(int id, String nomeTitular, String corretora) {
        validarId(id);
        validarNomeTitular(nomeTitular);
        validarCorretora(corretora);

        Carteira carteira = buscarPorId(id); // já valida existência
        carteira.setNomeTitular(nomeTitular.trim());
        carteira.setCorretora(corretora.trim());

        return carteiraDAO.atualizar(carteira);
    }

    /**
     * Remove uma carteira pelo seu ID.
     *
     * @param id ID da carteira a ser removida
     * @throws IllegalArgumentException se o ID for inválido
     * @throws RuntimeException         se a carteira não for encontrada
     */
    public void removerCarteira(int id) {
        validarId(id);
        buscarPorId(id); // garante que existe antes de tentar remover

        carteiraDAO.deletar(id);
    }

    // Métodos privados de validação
    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID da carteira deve ser maior que zero.");
        }
    }

    private void validarNomeTitular(String nomeTitular) {
        if (nomeTitular == null || nomeTitular.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do titular não pode ser vazio.");
        }
    }

    private void validarCorretora(String corretora) {
        if (corretora == null || corretora.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da corretora não pode ser vazio.");
        }
    }
}
