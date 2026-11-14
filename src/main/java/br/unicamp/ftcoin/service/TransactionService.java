package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dao.TransactionDAO;
import br.unicamp.ftcoin.dto.OperationType;
import br.unicamp.ftcoin.dto.Quote;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.util.ActivityLog;
import br.unicamp.ftcoin.dto.TransactionReceipt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Regras de negocio das compras e vendas. */
public class TransactionService {

    /** Espelha MOVIMENTACAO.Quantidade DECIMAL(10,3). */
    public static final int QUANTITY_SCALE = 3;

    /** Digitos inteiros da mesma coluna: DECIMAL(10,3) deixa 10 - 3. */
    public static final int QUANTITY_INTEGER_DIGITS = 7;

    /** Maior quantidade que a coluna aceita. */
    public static final BigDecimal MAX_QUANTITY = new BigDecimal("9999999.999");

    /**
     * Datas aceitas em uma movimentacao.
     *
     * <p>O teto existe por necessidade tecnica: sem ele uma data em ano distante
     * e aceita, e o relatorio tenta montar uma cotacao por dia do intervalo. Um
     * seculo cobre qualquer uso real e preserva data futura, que o servidor de
     * cotacoes atende.</p>
     *
     * <p>O piso e so uma barreira contra absurdo, e nao a cobertura do oraculo.
     * Quem sabe de que datas tem cotacao e a fonte: a formula local comeca em
     * 2026 e o servidor da faculdade responde desde 2025. Fixar o piso em 2026
     * recusaria aqui datas que o servidor atende, e a mensagem precisa de
     * cotacao ausente vem da propria fonte, em {@code OracleService.quoteOn},
     * que o registro consulta antes de gravar.</p>
     */
    public static final LocalDate MIN_DATE = LocalDate.of(2000, 1, 1);

    /** Fim do intervalo de datas aceito; ver {@link #MIN_DATE}. */
    public static final LocalDate MAX_DATE = LocalDate.of(2126, 12, 31);

    private final TransactionDAO transactionDAO;
    private final OracleService oracleService;

    public TransactionService(TransactionDAO transactionDAO, OracleService oracleService) {
        this.transactionDAO = transactionDAO;
        this.oracleService = oracleService;
    }

    /**
     * Registra uma compra.
     *
     * @throws BusinessException se a quantidade nao for positiva
     */
    public TransactionReceipt buy(int walletId, BigDecimal quantity, LocalDate date) {
        return register(walletId, quantity, date, OperationType.BUY);
    }

    /**
     * Registra uma venda.
     *
     * @throws BusinessException se a quantidade nao for positiva ou exceder o saldo
     */
    public TransactionReceipt sell(int walletId, BigDecimal quantity, LocalDate date) {
        BigDecimal normalized = normalize(quantity);
        // A data vem antes do saldo de proposito: uma venda em data fora do
        // intervalo tem saldo zero naquele dia e seria recusada por saldo
        // insuficiente, apontando o problema errado.
        requireAcceptableDate(date);
        requireChronologicallyPossible(walletId, normalized, date);
        return register(walletId, normalized, date, OperationType.SELL);
    }

    /**
     * Recusa a venda que deixaria o saldo negativo em algum dia da serie.
     *
     * <p>Conferir contra {@link #balanceOf(int)} nao basta, porque aquele saldo
     * e um total sem data: com uma compra em junho, uma venda lancada em janeiro
     * passava na validacao e so era descoberta pela apuracao, que percorre os
     * dias em ordem. Tambem nao basta conferir o saldo na data da venda, porque
     * uma venda retroativa aceitavel pode inviabilizar um dia posterior —
     * comprar 10 em 01/01, vender 10 em 01/03 e depois vender 5 em 01/02 passa
     * em qualquer verificacao pontual e quebra o dia 01/03. Dai simular a serie
     * inteira.</p>
     *
     * <p>A ordem reproduz a da apuracao, que e a da planilha: as operacoes de uma
     * data sao somadas e a compra do dia entra antes da venda.</p>
     *
     * <p>Custa uma leitura do historico da carteira por venda registrada, que em
     * modo relacional e uma ida ao banco. E o mesmo custo que a validacao
     * anterior ja tinha, por consultar o saldo.</p>
     *
     * @throws BusinessException se algum dia ficaria com saldo negativo
     */
    private void requireChronologicallyPossible(int walletId, BigDecimal quantity, LocalDate date) {
        Map<LocalDate, BigDecimal> bought = new TreeMap<>();
        Map<LocalDate, BigDecimal> sold = new TreeMap<>();
        for (Transaction transaction : transactionDAO.findByWallet(walletId)) {
            Map<LocalDate, BigDecimal> side = transaction.isBuy() ? bought : sold;
            side.merge(transaction.getDate(), transaction.getQuantity(), BigDecimal::add);
        }
        sold.merge(date, quantity, BigDecimal::add);

        Set<LocalDate> dates = new TreeSet<>(bought.keySet());
        dates.addAll(sold.keySet());
        BigDecimal balance = BigDecimal.ZERO;
        for (LocalDate day : dates) {
            balance = balance.add(bought.getOrDefault(day, BigDecimal.ZERO))
                    .subtract(sold.getOrDefault(day, BigDecimal.ZERO));
            if (balance.signum() < 0) {
                throw new BusinessException("transaction.msg.insufficientBalance");
            }
        }
    }

    private TransactionReceipt register(int walletId, BigDecimal quantity,
                                        LocalDate date, OperationType type) {
        BigDecimal normalized = normalize(quantity);
        requireAcceptableDate(date);
        // A cotacao vem antes de gravar. Com o oraculo em rede ela pode falhar, e
        // gravar primeiro deixaria a carteira com uma operacao que o usuario viu
        // ser recusada. Sem preco nao ha operacao.
        Quote quote = oracleService.quoteOn(date);
        Transaction transaction = new Transaction(0, walletId, date, type, normalized);
        if (!transactionDAO.insert(transaction)) {
            throw new BusinessException("transaction.msg.notRegistered");
        }
        ActivityLog.getInstance().tradeRegistered(
                type == OperationType.BUY ? "buy" : "sell", walletId, normalized, date);
        return new TransactionReceipt(transaction, quote, balanceOf(walletId));
    }

    /** Cotacao de uma data, para a opcao "cotar moeda virtual" do menu. */
    public Quote quoteOn(LocalDate date) {
        return oracleService.quoteOn(date);
    }

    /** Saldo em moeda virtual: compras menos vendas. */
    public BigDecimal balanceOf(int walletId) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction transaction : transactionDAO.findByWallet(walletId)) {
            balance = transaction.isBuy()
                    ? balance.add(transaction.getQuantity())
                    : balance.subtract(transaction.getQuantity());
        }
        return balance;
    }

    /** Relatorio 4: historico da carteira, em ordem cronologica. */
    public List<Transaction> historyOf(int walletId) {
        List<Transaction> history = new ArrayList<>(transactionDAO.findByWallet(walletId));
        history.sort(Comparator.comparing(Transaction::getDate).thenComparingInt(Transaction::getId));
        return history;
    }

    /** Ver {@link #MIN_DATE}. */
    private void requireAcceptableDate(LocalDate date) {
        if (date == null || date.isBefore(MIN_DATE) || date.isAfter(MAX_DATE)) {
            throw new BusinessException("transaction.msg.invalidDate");
        }
    }

    /**
     * Arredonda a quantidade para a escala da coluna e recusa o que ela nao
     * comporta.
     *
     * <p>A ordem das tres verificacoes importa. A magnitude e conferida por
     * contagem de digitos, antes de qualquer arredondamento: {@code 1e999999999}
     * tem precisao 1 e escala -999999999, de modo que a conta responde sem
     * materializar um inteiro de um bilhao de digitos, que e o que o
     * {@code setScale} tentaria fazer. E a positividade e afirmada <b>depois</b>
     * do arredondamento, sobre o valor que sera realmente gravado: {@code 0,0001}
     * e positivo antes dele e zero depois, e a versao anterior desta classe
     * gravava essa operacao com recibo de sucesso e quantidade {@code 0,000}.</p>
     *
     * @throws BusinessException se a quantidade for nula, nao couber na coluna
     *                           ou nao for positiva depois de arredondada
     */
    private BigDecimal normalize(BigDecimal quantity) {
        if (quantity == null) {
            throw new BusinessException("transaction.msg.invalidQuantity");
        }
        if (quantity.precision() - quantity.scale() > QUANTITY_INTEGER_DIGITS) {
            throw new BusinessException("transaction.msg.invalidQuantity");
        }
        BigDecimal normalized = quantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
        if (normalized.signum() <= 0 || normalized.compareTo(MAX_QUANTITY) > 0) {
            throw new BusinessException("transaction.msg.invalidQuantity");
        }
        return normalized;
    }
}
