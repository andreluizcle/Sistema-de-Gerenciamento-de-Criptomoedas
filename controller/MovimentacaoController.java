package controller;

import dto.Movimentacao;
import service.MovimentacaoService;
import view.MovimentacaoView;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador responsável por orquestrar a interação entre a View de
 * Movimentação e o Service de Movimentação.
 *
 * <p>Seguindo a arquitetura MVC + Service adotada pelo grupo, este Controller
 * atua apenas como um orquestrador: ele não contém regras de negócio (que ficam
 * no Service) nem operações de I/O (que ficam na View). Sua responsabilidade é
 * receber a opção escolhida pelo usuário, chamar o método apropriado da View
 * para coletar dados, repassar para o Service e devolver o resultado à View.</p>
 *
 * <p><b>Nota sobre a moeda:</b> de acordo com o enunciado do projeto (FT_Coin)
 * e a estrutura das tabelas Movimentação e Oráculo, o sistema gerencia uma
 * única moeda virtual. Portanto, a movimentação não exige nem armazena o
 * símbolo da moeda — apenas a carteira, a data, o tipo (Compra/Venda) e a
 * quantidade. O valor financeiro é derivado em tempo de relatório, consultando
 * o Oráculo para obter a cotação da data da operação.</p>
 *
 * @author Guilherme Gali Rocha
 */
public class MovimentacaoController {

    private final MovimentacaoView view;
    private final MovimentacaoService service;

    /**
     * Constrói o Controller com as dependências (View e Service) injetadas.
     *
     * @param view    Instância da View de Movimentação
     * @param service Instância do Service de Movimentação
     */
    public MovimentacaoController(MovimentacaoView view, MovimentacaoService service) {
        this.view = view;
        this.service = service;
    }

    /**
     * Loop principal do submenu de Movimentação. Permanece exibindo o menu
     * até que o usuário escolha voltar (opção 0).
     */
    public void executar() {
        int opcao;
        do {
            opcao = view.exibirMenu();
            try {
                switch (opcao) {
                    case 1:
                        registrarCompra();
                        break;
                    case 2:
                        registrarVenda();
                        break;
                    case 3:
                        listarMovimentacoesDaCarteira();
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
                // Regras de negócio violadas (ex: carteira não encontrada,
                // saldo insuficiente para venda)
                view.exibirErro(e.getMessage());
            }
        } while (opcao != 0);
    }

    /**
     * Coleta os dados de uma compra (carteira e quantidade) via View
     * e delega ao Service. A data da operação é definida automaticamente
     * como a data atual pelo Service.
     */
    private void registrarCompra() {
        int idCarteira = view.solicitarIdCarteira();
        BigDecimal quantidade = view.solicitarQuantidade();

        Movimentacao mov = service.registrarCompra(idCarteira, quantidade);
        view.exibirMovimentacaoRegistrada(mov);
    }

    /**
     * Coleta os dados de uma venda (carteira e quantidade) via View
     * e delega ao Service. O Service valida se há saldo suficiente
     * na carteira antes de registrar.
     */
    private void registrarVenda() {
        int idCarteira = view.solicitarIdCarteira();
        BigDecimal quantidade = view.solicitarQuantidade();

        Movimentacao mov = service.registrarVenda(idCarteira, quantidade);
        view.exibirMovimentacaoRegistrada(mov);
    }

    /**
     * Solicita o ID da carteira via View e lista todas as movimentações
     * (compras e vendas) associadas a ela. Útil para o usuário visualizar
     * o extrato/histórico antes de operações ou relatórios mais detalhados.
     */
    private void listarMovimentacoesDaCarteira() {
        int idCarteira = view.solicitarIdCarteira();
        List<Movimentacao> movimentacoes = service.listarPorCarteira(idCarteira);
        view.listarMovimentacoes(movimentacoes);
    }
}
