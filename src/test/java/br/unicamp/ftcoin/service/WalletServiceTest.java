package br.unicamp.ftcoin.service;

import br.unicamp.ftcoin.dao.MemoryTransactionDAO;
import br.unicamp.ftcoin.dao.MemoryWalletDAO;
import br.unicamp.ftcoin.dao.TransactionDAO;
import br.unicamp.ftcoin.dao.WalletDAO;
import br.unicamp.ftcoin.dto.OperationType;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.dto.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regras de negocio das carteiras. */
class WalletServiceTest {

    private WalletDAO walletDAO;
    private TransactionDAO transactionDAO;
    private WalletService service;

    @BeforeEach
    void setUp() {
        walletDAO = new MemoryWalletDAO();
        transactionDAO = new MemoryTransactionDAO();
        service = new WalletService(walletDAO, transactionDAO);
    }

    private Wallet add(String holder) {
        Wallet wallet = new Wallet(0, holder, "Corretora X");
        service.add(wallet);
        return wallet;
    }

    @Test
    @DisplayName("Titular e corretora sao obrigatorios")
    void rejectsMissingData() {
        assertThrows(BusinessException.class, () -> service.add(new Wallet(0, "", "Corretora")));
        assertThrows(BusinessException.class, () -> service.add(new Wallet(0, "Ana", "  ")));
        assertThrows(BusinessException.class, () -> service.add(new Wallet(0, null, "Corretora")));
        assertEquals(0, service.listById().size(), "nada deveria ter sido gravado");
    }

    @Test
    @DisplayName("Excluir a carteira exclui as movimentacoes dela, e so dela")
    void deleteCascadesToTransactions() {
        Wallet kept = add("Ana");
        Wallet removed = add("Bruno");
        transactionDAO.insert(new Transaction(0, removed.getId(), LocalDate.of(2026, 1, 1),
                OperationType.BUY, new BigDecimal("10")));
        transactionDAO.insert(new Transaction(0, kept.getId(), LocalDate.of(2026, 1, 1),
                OperationType.BUY, new BigDecimal("5")));

        assertTrue(service.delete(removed.getId()));

        assertNull(walletDAO.findById(removed.getId()), "carteira removida");
        assertEquals(0, transactionDAO.findByWallet(removed.getId()).size(), "movimentacoes removidas");
        assertEquals(1, transactionDAO.findByWallet(kept.getId()).size(), "as demais permanecem");
    }

    @Test
    @DisplayName("Relatorio 1: ordenado por identificador")
    void listsOrderedById() {
        add("Carla");
        add("Ana");
        add("Bruno");

        List<Wallet> wallets = service.listById();

        assertEquals(List.of(1, 2, 3), wallets.stream().map(Wallet::getId).toList());
    }

    @Test
    @DisplayName("Relatorio 2: ordenado por titular, respeitando acentos")
    void listsOrderedByHolder() {
        add("Zuniga");
        add("Ávila");
        add("Bueno");

        List<Wallet> wallets = service.listByHolder();

        // Comparando codigos de caractere, "Ávila" cairia depois de "Zuniga".
        assertEquals(List.of("Ávila", "Bueno", "Zuniga"),
                wallets.stream().map(Wallet::getHolderName).toList());
    }

    @Test
    @DisplayName("Atualizar carteira inexistente e recusado")
    void rejectsUpdateOfUnknownWallet() {
        assertThrows(BusinessException.class,
                () -> service.update(new Wallet(0, "Ana", "Corretora")));
    }
}
