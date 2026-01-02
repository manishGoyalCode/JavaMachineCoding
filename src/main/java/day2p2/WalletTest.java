package day2p2;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WalletTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new WalletImpl(); // 🔁 Replace with your implementation
    }

    // -------------------- Basic Initialization --------------------

    @Test
    void walletShouldStartWithZeroBalance() {
        assertEquals(0, wallet.getBalance());
        assertTrue(wallet.getTransactions().isEmpty());
    }

    // -------------------- Credit Tests --------------------

    @Test
    void creditShouldIncreaseBalanceAndCreateTransaction() {
        wallet.credit(100);

        assertEquals(100, wallet.getBalance());

        List<Transaction> txns = wallet.getTransactions();
        assertEquals(1, txns.size());
        assertEquals("CREDIT", txns.get(0).type);
        assertEquals(100, txns.get(0).amount);
    }

    @Test
    void multipleCreditsShouldAccumulateCorrectly() {
        wallet.credit(50);
        wallet.credit(70);

        assertEquals(120, wallet.getBalance());
        assertEquals(2, wallet.getTransactions().size());
    }

    @Test
    void creditZeroShouldFailOrDoNothing() {
        wallet.credit(0);

        assertEquals(0, wallet.getBalance());
        assertTrue(wallet.getTransactions().isEmpty());
    }

    @Test
    void creditNegativeAmountShouldFail() {
        wallet.credit(-100);

        assertEquals(0, wallet.getBalance());
        assertTrue(wallet.getTransactions().isEmpty());
    }

    // -------------------- Debit Tests --------------------

    @Test
    void debitShouldReduceBalanceAndCreateTransaction() {
        wallet.credit(100);
        boolean success = wallet.debit(40);

        assertTrue(success);
        assertEquals(60, wallet.getBalance());

        List<Transaction> txns = wallet.getTransactions();
        assertEquals(2, txns.size());
        assertEquals("DEBIT", txns.get(1).type);
        assertEquals(40, txns.get(1).amount);
    }

    @Test
    void debitExactBalanceShouldSucceed() {
        wallet.credit(100);
        boolean success = wallet.debit(100);

        assertTrue(success);
        assertEquals(0, wallet.getBalance());
        assertEquals(2, wallet.getTransactions().size());
    }

    @Test
    void debitMoreThanBalanceShouldFailAndCreateNoTransaction() {
        wallet.credit(100);
        boolean success = wallet.debit(150);

        assertFalse(success);
        assertEquals(100, wallet.getBalance());
        assertEquals(1, wallet.getTransactions().size());
    }

    @Test
    void debitWithoutAnyBalanceShouldFail() {
        boolean success = wallet.debit(10);

        assertFalse(success);
        assertEquals(0, wallet.getBalance());
        assertTrue(wallet.getTransactions().isEmpty());
    }

    @Test
    void debitZeroShouldFail() {
        wallet.credit(100);
        boolean success = wallet.debit(0);

        assertFalse(success);
        assertEquals(100, wallet.getBalance());
        assertEquals(1, wallet.getTransactions().size());
    }

    @Test
    void debitNegativeAmountShouldFail() {
        wallet.credit(100);
        boolean success = wallet.debit(-50);

        assertFalse(success);
        assertEquals(100, wallet.getBalance());
        assertEquals(1, wallet.getTransactions().size());
    }

    // -------------------- Transaction Rules --------------------

    @Test
    void failedDebitShouldNotCreateTransaction() {
        wallet.credit(100);
        wallet.debit(200);

        List<Transaction> txns = wallet.getTransactions();
        assertEquals(1, txns.size());
        assertEquals("CREDIT", txns.get(0).type);
    }

    @Test
    void transactionsShouldBeInCorrectOrder() {
        wallet.credit(100);
        wallet.debit(30);
        wallet.credit(20);

        List<Transaction> txns = wallet.getTransactions();

        assertEquals("CREDIT", txns.get(0).type);
        assertEquals("DEBIT", txns.get(1).type);
        assertEquals("CREDIT", txns.get(2).type);
    }

    @Test
    void transactionTimestampsShouldBeNonDecreasing() {
        wallet.credit(100);
        wallet.debit(10);
        wallet.credit(5);

        List<Transaction> txns = wallet.getTransactions();

        for (int i = 1; i < txns.size(); i++) {
            assertTrue(
                    txns.get(i).timestamp >= txns.get(i - 1).timestamp,
                    "Timestamps must be non-decreasing"
            );
        }
    }

    // -------------------- Immutability Tests --------------------

    @Test
    void transactionListShouldBeImmutable() {
        wallet.credit(100);
        List<Transaction> txns = wallet.getTransactions();

        assertThrows(
                UnsupportedOperationException.class,
                () -> txns.add(new Transaction("CREDIT", 50, System.currentTimeMillis()))
        );
    }

    @Test
    void transactionObjectsShouldBeImmutable() {
        wallet.credit(100);
        Transaction txn = wallet.getTransactions().get(0);

        assertEquals("CREDIT", txn.type);
        assertEquals(100, txn.amount);
    }

    // -------------------- Stress / Boundary Tests --------------------

    @Test
    void largeAmountCreditShouldWork() {
        long largeAmount = Long.MAX_VALUE / 2;

        wallet.credit(largeAmount);
        assertEquals(largeAmount, wallet.getBalance());
    }

    @Test
    void debitAfterLargeCreditShouldWork() {
        long largeAmount = 1_000_000_000L;

        wallet.credit(largeAmount);
        wallet.debit(500_000_000L);

        assertEquals(500_000_000L, wallet.getBalance());
    }
}
