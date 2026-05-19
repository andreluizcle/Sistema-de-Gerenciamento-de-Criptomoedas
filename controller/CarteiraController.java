package controller;

import dto.Carteira;
import service.CarteiraService;
import view.CarteiraView;

import java.util.List;

/**
 * Controlador responsável por orquestrar a interação entre a View de Carteira
 * e o Service de Carteira.
 *
 * <p>Seguindo a arquitetura MVC + Service adotada pelo grupo, este Controller
 * atua apenas como um orquestrador: ele não contém regras de negócio (que ficam
 * no Service) nem operações de I/O (que ficam na View). Sua responsabilidade é
 * receber a opção escolhida pelo usuário, chamar o método apropriado da View
 * para coletar dados, repassar para o Service e devolver o resultado à View.</p>
 *
 * <p>Trata as exceções lançadas pelo Service ({@link IllegalArgumentException}
 * para dados inválidos e {@link RuntimeException} para regras de negócio
 * violadas) e as transforma em mensagens amigáveis exibidas pela View.</p>
 *
 * @author Guilherme Gali Rocha
 */
public class CarteiraController {

    private final CarteiraView view;
    private final CarteiraService service;

    /**
     * Constrói o Controller com as dependências (View e Service) injetadas.
     * A injeção de dependência via construtor evita o acoplamento forte com
     * implementações concretas e facilita testes unitários.
     *
     * @param view    Instância da View de Carteira
     * @param service Instância do Service de Carteira
     */
    public CarteiraController(CarteiraView view, CarteiraService service) {
        this.view = view;
        this.service = service;
    }

    /**
     * Loop principal do submenu de Carteira. Permanece exibindo o menu até
     * que o usuário escolha voltar (opção 0). Cada opção é tratada em um
     * método privado dedicado.
     */
    public void executar() {
        int opcao;
        do {
            opcao = view.exibirMenu();
            try {
                switch (opcao) {
                    case 1:
                        incluirCarteira();
                        break;
                    case 2:
                        consultarCarteira();
                        break;
                    case 3:
                        editarCarteira();
                        break;
                    case 4:
                        excluirCarteira();
                        break;
                    case 5:
                        listarCarteiras();
                        break;
                    case 0:
                        // Sai do loop
                        break;
                    default:
                        view.exibirMensagemInvalida();
                }
            } catch (IllegalArgumentException e) {
                // Dados inválidos fornecidos pelo usuário (validação do Service)
                view.exibirErro(e.getMessage());
            } catch (RuntimeException e) {
                // Regras de negócio violadas (ex: carteira não encontrada)
                view.exibirErro(e.getMessage());
            }
        } while (opcao != 0);
    }

    /**
     * Coleta os dados de uma nova carteira via View, chama o Service para
     * criar e persistir, e devolve o resultado à View.
     */
    private void incluirCarteira() {
        String nomeTitular = view.solicitarNomeTitular();
        String corretora = view.solicitarCorretora();

        Carteira carteiraCriada = service.criarCarteira(nomeTitular, corretora);
        view.exibirCarteiraCriada(carteiraCriada);
    }

    /**
     * Solicita o ID via View, busca a carteira no Service e devolve à View
     * para exibição detalhada.
     */
    private void consultarCarteira() {
        int id = view.solicitarIdCarteira();
        Carteira carteira = service.buscarPorId(id);
        view.exibirCarteira(carteira);
    }

    /**
     * Solicita o ID e os novos dados via View, e chama o Service para
     * atualizar. O Service garante que a carteira existe antes de atualizar.
     */
    private void editarCarteira() {
        int id = view.solicitarIdCarteira();
        // Exibe os dados atuais antes de editar (UX: usuário precisa saber
        // o que está editando antes de digitar os novos valores)
        Carteira atual = service.buscarPorId(id);
        view.exibirCarteira(atual);

        String novoNome = view.solicitarNomeTitular();
        String novaCorretora = view.solicitarCorretora();

        Carteira atualizada = service.atualizarCarteira(id, novoNome, novaCorretora);
        view.exibirCarteiraAtualizada(atualizada);
    }

    /**
     * Solicita o ID via View, pede confirmação ao usuário, e remove a carteira
     * via Service caso confirmado.
     */
    private void excluirCarteira() {
        int id = view.solicitarIdCarteira();
        Carteira carteira = service.buscarPorId(id);
        view.exibirCarteira(carteira);

        boolean confirmacao = view.confirmarExclusao();
        if (confirmacao) {
            service.removerCarteira(id);
            view.exibirSucesso();
        } else {
            view.exibirOperacaoCancelada();
        }
    }

    /**
     * Lista todas as carteiras cadastradas (sem ordenação aqui — relatórios
     * ordenados são responsabilidade do RelatorioController).
     */
    private void listarCarteiras() {
        List<Carteira> carteiras = service.listarTodas();
        view.listarCarteiras(carteiras);
    }
}
