package day2p2;

public class Transaction {
    public final String type;
    public final long amount;
    public final long timestamp;

    public Transaction(String type, long amount, long timestamp) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}
