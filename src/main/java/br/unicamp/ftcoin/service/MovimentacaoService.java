package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dao.interfaces.IMovimentacaoDAO;
import br.unicamp.ftcoin.dto.Movimentacao;
import br.unicamp.ftcoin.dto.Oraculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Camada de servico de Movimentacao.
 * Encapsula as regras de compra e venda de moeda virtual, calculo de saldo,
 * custo total e apuracao de ganhos / perdas em moeda real.
 */
public class MovimentacaoService {

    private final IMovimentacaoDAO movimentacaoDAO;
    private final OraculoService oraculoService;

    public MovimentacaoService(IMovimentacaoDAO movimentacaoDAO, OraculoService oraculoService) {
        this.movimentacaoDAO = movimentacaoDAO;
        this.oraculoService = oraculoService;
    }

    /** Registra compra de moeda virtual. */
    public Movimentacao registrarCompra(int idCarteira, BigDecimal quantidade, LocalDate data) {
        if (quantidade == null || quantidade.signum() <= 0) {
            return null;
        }
        Movimentacao mov = new Movimentacao(
                movimentacaoDAO.proximoId(),
                idCarteira,
                data == null ? LocalDate.now() : data,
                Movimentacao.COMPRA,
                quantidade
        );
        boolean ok = movimentacaoDAO.inserir(mov);
        return ok ? mov : null;
    }

    /** Registra venda. Verifica se ha saldo suficiente em moeda virtual. */
    public Movimentacao registrarVenda(int idCarteira, BigDecimal quantidade, LocalDate data) {
        if (quantidade == null || quantidade.signum() <= 0) {
            return null;
        }
        BigDecimal saldo = calcularSaldo(idCarteira);
        if (saldo.compareTo(quantidade) < 0) {
            return null;
        }
        Movimentacao mov = new Movimentacao(
                movimentacaoDAO.proximoId(),
                idCarteira,
                data == null ? LocalDate.now() : data,
                Movimentacao.VENDA,
                quantidade
        );
        boolean ok = movimentacaoDAO.inserir(mov);
        return ok ? mov : null;
    }

    /** Retorna o saldo em moeda virtual da carteira. */
    public BigDecimal calcularSaldo(int idCarteira) {
        BigDecimal saldo = BigDecimal.ZERO;
        for (Movimentacao m : movimentacaoDAO.listarPorCarteira(idCarteira)) {
            if (m.isCompra()) {
                saldo = saldo.add(m.getQuantidade());
            } else {
                saldo = saldo.subtract(m.getQuantidade());
            }
        }
        return saldo;
    }

    public List<Movimentacao> historico(int idCarteira) {
        List<Movimentacao> lista = movimentacaoDAO.listarPorCarteira(idCarteira);
        lista.sort(Comparator
                .comparing(Movimentacao::getDataOperacao)
                .thenComparingInt(Movimentacao::getIdMovimento));
        return lista;
    }

    /**
     * Apura o ganho ou perda total da carteira.
     * O custo total considera o valor pago nas compras (usando a cotacao da data
     * de cada compra) menos o valor recebido nas vendas (usando a cotacao da
     * data de cada venda). O valor atual considera o saldo restante avaliado
     * pela cotacao mais recente do oraculo.
     *
     * Resultado = (valor recebido nas vendas + valor atual do saldo) - valor pago nas compras
     */
    public BigDecimal calcularGanhoOuPerda(int idCarteira) {
        BigDecimal custoCompras = BigDecimal.ZERO;
        BigDecimal receitaVendas = BigDecimal.ZERO;

        for (Movimentacao m : movimentacaoDAO.listarPorCarteira(idCarteira)) {
            Oraculo cot = oraculoService.cotacaoEm(m.getDataOperacao());
            if (cot == null) {
                continue;
            }
            BigDecimal valor = m.getQuantidade().multiply(cot.getCotacao());
            if (m.isCompra()) {
                custoCompras = custoCompras.add(valor);
            } else {
                receitaVendas = receitaVendas.add(valor);
            }
        }

        BigDecimal saldoRestante = calcularSaldo(idCarteira);
        BigDecimal valorAtual = BigDecimal.ZERO;
        Oraculo cotacaoAtual = oraculoService.cotacaoMaisRecente();
        if (cotacaoAtual != null) {
            valorAtual = saldoRestante.multiply(cotacaoAtual.getCotacao());
        }

        return receitaVendas.add(valorAtual).subtract(custoCompras);
    }

    /** Valor atual em moeda real do saldo da carteira. */
    public BigDecimal valorAtual(int idCarteira) {
        BigDecimal saldo = calcularSaldo(idCarteira);
        Oraculo cotacaoAtual = oraculoService.cotacaoMaisRecente();
        if (cotacaoAtual == null) {
            return BigDecimal.ZERO;
        }
        return saldo.multiply(cotacaoAtual.getCotacao());
    }
}
