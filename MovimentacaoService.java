package service;

import dao.interface.IMovimentacaoDAO;
import dto.Movimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MovimentacaoService {

    private final IMovimentacaoDAO movimentacaoDAO;

    // Injeção de dependência via construtor — o Service não instancia o DAO,
    // apenas depende da sua interface.
    public MovimentacaoService(IMovimentacaoDAO movimentacaoDAO) {
        this.movimentacaoDAO = movimentacaoDAO;
    }

    /**
     * Registra uma compra de moeda para uma carteira.
     *
     * @param idCarteira  ID da carteira que realizou a compra
     * @param moeda       Símbolo da moeda (ex: "BTC", "ETH")
     * @param quantidade  Quantidade comprada
     * @param valorUnitario Valor unitário no momento da compra
     * @return A movimentação registrada
     * @throws IllegalArgumentException se os dados forem inválidos
     */
    public Movimentacao registrarCompra(int idCarteira, String moeda,
                                        BigDecimal quantidade, BigDecimal valorUnitario) {
        validarIdCarteira(idCarteira);
        validarMoeda(moeda);
        validarQuantidade(quantidade);
        validarValorUnitario(valorUnitario);

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setIdCarteira(idCarteira);
        movimentacao.setMoeda(moeda.trim().toUpperCase());
        movimentacao.setQuantidade(quantidade);
        movimentacao.setValorUnitario(valorUnitario);
        movimentacao.setTipo(Movimentacao.Tipo.COMPRA);
        movimentacao.setDataHora(LocalDateTime.now());

        return movimentacaoDAO.salvar(movimentacao);
    }

    /**
     * Registra uma venda de moeda de uma carteira.
     *
     * @param idCarteira    ID da carteira que realizou a venda
     * @param moeda         Símbolo da moeda (ex: "BTC", "ETH")
     * @param quantidade    Quantidade vendida
     * @param valorUnitario Valor unitário no momento da venda
     * @return A movimentação registrada
     * @throws IllegalArgumentException se os dados forem inválidos
     * @throws RuntimeException         se a carteira não tiver saldo suficiente
     */
    public Movimentacao registrarVenda(int idCarteira, String moeda,
                                       BigDecimal quantidade, BigDecimal valorUnitario) {
        validarIdCarteira(idCarteira);
        validarMoeda(moeda);
        validarQuantidade(quantidade);
        validarValorUnitario(valorUnitario);

        String moedaNormalizada = moeda.trim().toUpperCase();
        validarSaldoSuficiente(idCarteira, moedaNormalizada, quantidade);

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setIdCarteira(idCarteira);
        movimentacao.setMoeda(moedaNormalizada);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setValorUnitario(valorUnitario);
        movimentacao.setTipo(Movimentacao.Tipo.VENDA);
        movimentacao.setDataHora(LocalDateTime.now());

        return movimentacaoDAO.salvar(movimentacao);
    }

    /**
     * Busca uma movimentação pelo seu ID.
     *
     * @param id Identificador da movimentação
     * @return A movimentação encontrada
     * @throws IllegalArgumentException se o ID for inválido
     * @throws RuntimeException         se a movimentação não for encontrada
     */
    public Movimentacao buscarPorId(int id) {
        validarId(id);

        Movimentacao movimentacao = movimentacaoDAO.buscarPorId(id);
        if (movimentacao == null) {
            throw new RuntimeException("Movimentação de ID " + id + " não encontrada.");
        }

        return movimentacao;
    }

    /**
     * Lista todas as movimentações de uma carteira.
     *
     * @param idCarteira ID da carteira
     * @return Lista de movimentações (pode ser vazia, nunca null)
     * @throws IllegalArgumentException se o ID for inválido
     */
    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        validarIdCarteira(idCarteira);
        return movimentacaoDAO.listarPorCarteira(idCarteira);
    }

    /**
     * Lista todas as movimentações de uma carteira filtradas por moeda.
     *
     * @param idCarteira ID da carteira
     * @param moeda      Símbolo da moeda (ex: "BTC", "ETH")
     * @return Lista de movimentações filtradas (pode ser vazia, nunca null)
     * @throws IllegalArgumentException se os dados forem inválidos
     */
    public List<Movimentacao> listarPorCarteiraEMoeda(int idCarteira, String moeda) {
        validarIdCarteira(idCarteira);
        validarMoeda(moeda);
        return movimentacaoDAO.listarPorCarteiraEMoeda(idCarteira, moeda.trim().toUpperCase());
    }

    /**
     * Calcula o saldo atual de uma moeda em uma carteira,
     * somando compras e subtraindo vendas.
     *
     * @param idCarteira ID da carteira
     * @param moeda      Símbolo da moeda
     * @return Saldo atual da moeda na carteira
     * @throws IllegalArgumentException se os dados forem inválidos
     */
    public BigDecimal calcularSaldo(int idCarteira, String moeda) {
        validarIdCarteira(idCarteira);
        validarMoeda(moeda);

        List<Movimentacao> movimentacoes =
                listarPorCarteiraEMoeda(idCarteira, moeda.trim().toUpperCase());

        return movimentacoes.stream()
                .map(m -> m.getTipo() == Movimentacao.Tipo.COMPRA
                        ? m.getQuantidade()
                        : m.getQuantidade().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Remove uma movimentação pelo seu ID.
     *
     * @param id ID da movimentação a ser removida
     * @throws IllegalArgumentException se o ID for inválido
     * @throws RuntimeException         se a movimentação não for encontrada
     */
    public void removerMovimentacao(int id) {
        validarId(id);
        buscarPorId(id); // garante que existe antes de tentar remover

        movimentacaoDAO.deletar(id);
    }

    // Métodos privados de validação
    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID da movimentação deve ser maior que zero.");
        }
    }

    private void validarIdCarteira(int idCarteira) {
        if (idCarteira <= 0) {
            throw new IllegalArgumentException("ID da carteira deve ser maior que zero.");
        }
    }

    private void validarMoeda(String moeda) {
        if (moeda == null || moeda.trim().isEmpty()) {
            throw new IllegalArgumentException("O símbolo da moeda não pode ser vazio.");
        }
    }

    private void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }

    private void validarValorUnitario(BigDecimal valorUnitario) {
        if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor unitário deve ser maior que zero.");
        }
    }

    private void validarSaldoSuficiente(int idCarteira, String moeda, BigDecimal quantidadeDesejada) {
        BigDecimal saldoAtual = calcularSaldo(idCarteira, moeda);
        if (saldoAtual.compareTo(quantidadeDesejada) < 0) {
            throw new RuntimeException(
                    "Saldo insuficiente de " + moeda + ". Saldo atual: " + saldoAtual
                    + ", quantidade solicitada: " + quantidadeDesejada + ".");
        }
    }
}
