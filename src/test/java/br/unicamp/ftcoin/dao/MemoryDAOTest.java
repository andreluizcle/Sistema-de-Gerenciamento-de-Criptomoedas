package br.unicamp.ftcoin.dao;

import br.unicamp.ftcoin.dto.OperationType;
import br.unicamp.ftcoin.dto.Transaction;
import br.unicamp.ftcoin.dto.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Persistencia em memoria, que e tambem a usada nos testes e na demonstracao. */
class MemoryDAOTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 1);

    private Wallet newWallet(String holder) {
        return new Wallet(0, holder, "Corretora X");
    }

    private Transaction newTransaction(int walletId, String quantity) {
        return new Transaction(0, walletId, DAY, OperationType.BUY, new BigDecimal(quantity));
    }

    @Test
    @DisplayName("A insercao atribui identificadores sequenciais a partir de 1")
    void insertAssignsSequentialIds() {
        MemoryWalletDAO dao = new MemoryWalletDAO();
        Wallet first = newWallet("Ana");
        Wallet second = newWallet("Bruno");

        assertTrue(dao.insert(first));
        assertTrue(dao.insert(second));

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
    }

    @Test
    @DisplayName("Consultar identificador inexistente devolve nulo")
    void findByIdReturnsNullWhenMissing() {
        assertNull(new MemoryWalletDAO().findById(42));
    }

    @Test
    @DisplayName("Atualizar carteira inexistente nao faz nada")
    void updateMissingWalletFails() {
        MemoryWalletDAO dao = new MemoryWalletDAO();
        assertFalse(dao.update(new Wallet(99, "Fantasma", "X")));
    }

    @Test
    @DisplayName("Excluir remove a carteira")
    void deleteRemovesTheWallet() {
        MemoryWalletDAO dao = new MemoryWalletDAO();
        Wallet wallet = newWallet("Ana");
        dao.insert(wallet);

        assertTrue(dao.delete(wallet.getId()));
        assertNull(dao.findById(wallet.getId()));
        assertFalse(dao.delete(wallet.getId()), "excluir duas vezes");
    }

    @Test
    @DisplayName("Alterar o objeto depois de inserir nao altera o que foi gravado")
    void storesACopyOnInsert() {
        // Sem copia defensiva o mapa seria apenas um apelido para o objeto do
        // chamador, e uma edicao recusada pela validacao ficaria gravada assim mesmo.
        MemoryWalletDAO dao = new MemoryWalletDAO();
        Wallet wallet = newWallet("Ana");
        dao.insert(wallet);

        wallet.setHolderName("Nome trocado fora do DAO");

        assertEquals("Ana", dao.findById(wallet.getId()).getHolderName());
    }

    @Test
    @DisplayName("Alterar o objeto devolvido na consulta nao altera o que esta gravado")
    void returnsACopyOnRead() {
        MemoryWalletDAO dao = new MemoryWalletDAO();
        Wallet wallet = newWallet("Ana");
        dao.insert(wallet);

        Wallet read = dao.findById(wallet.getId());
        read.setHolderName("Nome trocado fora do DAO");

        assertEquals("Ana", dao.findById(wallet.getId()).getHolderName());
    }

    @Test
    @DisplayName("A lista devolvida pode ser alterada sem afetar o armazenamento")
    void findAllReturnsADetachedList() {
        MemoryWalletDAO dao = new MemoryWalletDAO();
        dao.insert(newWallet("Ana"));

        List<Wallet> wallets = dao.findAll();
        wallets.clear();

        assertEquals(1, dao.findAll().size());
    }

    @Test
    @DisplayName("Movimentacoes sao filtradas pela carteira")
    void findsTransactionsByWallet() {
        MemoryTransactionDAO dao = new MemoryTransactionDAO();
        dao.insert(newTransaction(1, "10"));
        dao.insert(newTransaction(1, "20"));
        dao.insert(newTransaction(2, "30"));

        assertEquals(2, dao.findByWallet(1).size());
        assertEquals(1, dao.findByWallet(2).size());
        assertEquals(0, dao.findByWallet(3).size());
    }

    @Test
    @DisplayName("Excluir por carteira devolve quantas linhas saiu e poupa as demais")
    void deleteByWalletReturnsTheRowCount() {
        MemoryTransactionDAO dao = new MemoryTransactionDAO();
        dao.insert(newTransaction(1, "10"));
        dao.insert(newTransaction(1, "20"));
        Transaction other = newTransaction(2, "30");
        dao.insert(other);

        assertEquals(2, dao.deleteByWallet(1), "linhas removidas");
        assertEquals(0, dao.deleteByWallet(1), "nada mais a remover");
        assertNotNull(dao.findById(other.getId()), "a carteira 2 nao foi tocada");
    }
}
