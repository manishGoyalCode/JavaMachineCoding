package day2p2;

import java.util.List;

public interface Wallet {
    void credit(long amount);
    boolean debit(long amount);
    long getBalance();
    List<Transaction> getTransactions();
}
