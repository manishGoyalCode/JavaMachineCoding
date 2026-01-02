package day2p2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WalletImpl implements Wallet {
    List<Transaction> transactions;
    long balance;

    public WalletImpl() {
        this.balance = 0;
        this.transactions = new ArrayList<>();
    }

    @Override
    public synchronized boolean debit(long amount) {
        if (amount <= 0) {
            return false;
        }
        if (amount > balance) {
            return false;
        }
        balance = balance - amount;
        transactions.add(new Transaction("DEBIT", amount, Instant.now().toEpochMilli()));
        return true;
    }

    @Override
    public synchronized void credit(long amount) {
        if (amount <= 0) {
            return;
        }
        balance = balance + amount;
        transactions.add(new Transaction("CREDIT", amount, Instant.now().toEpochMilli()));
    }

    @Override
    public synchronized long getBalance() {
        return balance;
    }

    @Override
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions) ;
    }
}
