/**
 * Transaction.java
 * 
 * Purpose:
 * Represents a single financial transaction (Deposit, Withdrawal, or Transfer).
 * Records the transaction type, monetary amount, timestamp, related account number,
 * and optional remarks (e.g. transfer destination).
 * 
 * Target Audience: 2nd-Year Engineering Students learning Core Java & OOP Concepts.
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    // Transaction details
    private String transactionId;
    private String type;           // "Deposit", "Withdraw", or "Transfer"
    private double amount;
    private String date;           // Formatted date string
    private String accountNumber;  // Account associated with this transaction
    private String remarks;        // Additional note (e.g., "To Acc: 1002")

    // Formatter for readable date-time presentation
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Parameterized Constructor when automatic timestamping is desired.
     * 
     * @param type          "Deposit", "Withdraw", or "Transfer"
     * @param amount        Transaction monetary value
     * @param accountNumber Account number associated
     * @param remarks       Additional context / remarks
     */
    public Transaction(String type, double amount, String accountNumber, String remarks) {
        this.transactionId = "TXN" + System.currentTimeMillis() % 100000;
        this.type = type;
        this.amount = amount;
        this.date = LocalDateTime.now().format(FORMATTER);
        this.accountNumber = accountNumber;
        this.remarks = remarks;
    }

    /**
     * Parameterized Constructor when date is explicitly provided (e.g. if loaded from storage).
     */
    public Transaction(String transactionId, String type, double amount, String date, String accountNumber, String remarks) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.accountNumber = accountNumber;
        this.remarks = remarks;
    }

    // ==================== GETTERS AND SETTERS ====================

    public String getTransactionId() {
        return transactionId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    /**
     * Formats transaction as a single structured table row for mini statements.
     */
    @Override
    public String toString() {
        return String.format("%-10s | %-19s | %-10s | $%-9.2f | %s",
            transactionId, date, type, amount, remarks);
    }
}
