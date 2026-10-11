/**
 * Bank.java
 * 
 * Purpose:
 * Core business logic engine for the Bank Management System.
 * Manages the collection of active accounts and transaction logs.
 * Enforces business rules and validations (e.g. balance checks, PIN authentication,
 * duplicate account detection, fund transfer rules, and mini statement generation).
 * 
 * Target Audience: 2nd-Year Engineering Students learning Core Java & Business Logic.
 */

import java.util.ArrayList;
import java.util.List;

public class Bank {
    // In-memory data structures using Java Collections
    private ArrayList<Account> accounts;
    private ArrayList<Transaction> transactions;

    /**
     * Default Constructor.
     */
    public Bank() {
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    /**
     * Constructor initialized with preloaded accounts.
     * 
     * @param initialAccounts List of accounts loaded from storage
     */
    public Bank(ArrayList<Account> initialAccounts) {
        this.accounts = (initialAccounts != null) ? initialAccounts : new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    // ==================== GETTERS ====================

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    // ==================== ACCOUNT OPERATIONS ====================

    /**
     * Searches for an account by its unique account number.
     * 
     * @param accountNumber Account number to search for
     * @return Account object if found, or null if not found
     */
    public Account findAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return null;
        }
        for (Account acc : accounts) {
            if (acc.getAccountNumber().equalsIgnoreCase(accountNumber.trim())) {
                return acc;
            }
        }
        return null;
    }

    /**
     * Creates and registers a new account after validating inputs.
     * 
     * @param accountNumber  Desired unique account number
     * @param holderName     Name of the account holder
     * @param pin            4-digit security PIN
     * @param initialDeposit Opening balance (must be non-negative)
     * @return true if created successfully, false otherwise
     */
    public boolean createAccount(String accountNumber, String holderName, String pin, double initialDeposit) {
        // Validation: Blank inputs
        if (accountNumber == null || accountNumber.trim().isEmpty() ||
            holderName == null || holderName.trim().isEmpty() ||
            pin == null || pin.trim().isEmpty()) {
            System.out.println("[Error] Account number, holder name, and PIN cannot be empty.");
            return false;
        }

        // Validation: Unique account number
        if (findAccount(accountNumber) != null) {
            System.out.println("[Error] Account number '" + accountNumber + "' already exists! Please choose another.");
            return false;
        }

        // Validation: Non-negative initial deposit
        if (initialDeposit < 0) {
            System.out.println("[Error] Initial deposit cannot be negative.");
            return false;
        }

        // Create and add account
        Account newAcc = new Account(accountNumber.trim(), holderName.trim(), pin.trim(), initialDeposit);
        accounts.add(newAcc);

        // Record initial deposit transaction if amount > 0
        if (initialDeposit > 0) {
            transactions.add(new Transaction("Deposit", initialDeposit, newAcc.getAccountNumber(), "Initial Opening Deposit"));
        }

        System.out.println("\n[Success] Account successfully created for " + holderName + "!");
        System.out.println(newAcc);
        return true;
    }

    /**
     * Authenticates account credentials.
     * 
     * @param accountNumber Account number
     * @param pin           Security PIN
     * @return Account object if login is successful, null otherwise
     */
    public Account login(String accountNumber, String pin) {
        Account acc = findAccount(accountNumber);
        if (acc == null) {
            System.out.println("[Error] Account number '" + accountNumber + "' not found.");
            return null;
        }

        if (!acc.validatePin(pin)) {
            System.out.println("[Error] Invalid PIN entered for account: " + accountNumber);
            return null;
        }

        System.out.println("[Success] Login successful. Welcome back, " + acc.getHolderName() + "!");
        return acc;
    }

    /**
     * Deposits money into a specified account.
     * 
     * @param accountNumber Target account number
     * @param amount        Amount to deposit (must be > 0)
     * @return true if deposit succeeds, false otherwise
     */
    public boolean deposit(String accountNumber, double amount) {
        if (amount <= 0) {
            System.out.println("[Error] Deposit amount must be greater than zero.");
            return false;
        }

        Account acc = findAccount(accountNumber);
        if (acc == null) {
            System.out.println("[Error] Account not found: " + accountNumber);
            return false;
        }

        acc.credit(amount);
        transactions.add(new Transaction("Deposit", amount, acc.getAccountNumber(), "Cash / Online Deposit"));

        System.out.printf("\n[Success] Successfully deposited $%.2f into Account %s.\n", amount, acc.getAccountNumber());
        System.out.printf("Updated Balance: $%.2f\n", acc.getBalance());
        return true;
    }

    /**
     * Withdraws money from an account after validating PIN and sufficient funds.
     * 
     * @param accountNumber Account number
     * @param pin           Security PIN
     * @param amount        Amount to withdraw (must be > 0 and <= balance)
     * @return true if withdrawal succeeds, false otherwise
     */
    public boolean withdraw(String accountNumber, String pin, double amount) {
        if (amount <= 0) {
            System.out.println("[Error] Withdrawal amount must be greater than zero.");
            return false;
        }

        Account acc = findAccount(accountNumber);
        if (acc == null) {
            System.out.println("[Error] Account not found: " + accountNumber);
            return false;
        }

        if (!acc.validatePin(pin)) {
            System.out.println("[Error] Incorrect PIN! Withdrawal denied.");
            return false;
        }

        if (acc.getBalance() < amount) {
            System.out.printf("[Error] Insufficient funds! Available balance: $%.2f, Requested: $%.2f\n",
                acc.getBalance(), amount);
            return false;
        }

        acc.debit(amount);
        transactions.add(new Transaction("Withdraw", amount, acc.getAccountNumber(), "ATM / Branch Withdrawal"));

        System.out.printf("\n[Success] Successfully withdrew $%.2f from Account %s.\n", amount, acc.getAccountNumber());
        System.out.printf("Remaining Balance: $%.2f\n", acc.getBalance());
        return true;
    }

    /**
     * Transfers money between two accounts with security verification.
     * 
     * @param fromAccNo Source account number
     * @param fromPin   Source account PIN
     * @param toAccNo   Destination account number
     * @param amount    Amount to transfer (must be > 0 and <= sender balance)
     * @return true if transfer succeeds, false otherwise
     */
    public boolean transfer(String fromAccNo, String fromPin, String toAccNo, double amount) {
        if (amount <= 0) {
            System.out.println("[Error] Transfer amount must be greater than zero.");
            return false;
        }

        if (fromAccNo.equalsIgnoreCase(toAccNo)) {
            System.out.println("[Error] Sender and receiver account numbers cannot be the same!");
            return false;
        }

        Account sender = findAccount(fromAccNo);
        if (sender == null) {
            System.out.println("[Error] Source account not found: " + fromAccNo);
            return false;
        }

        if (!sender.validatePin(fromPin)) {
            System.out.println("[Error] Incorrect sender PIN! Transfer aborted.");
            return false;
        }

        Account receiver = findAccount(toAccNo);
        if (receiver == null) {
            System.out.println("[Error] Beneficiary account not found: " + toAccNo);
            return false;
        }

        if (sender.getBalance() < amount) {
            System.out.printf("[Error] Insufficient balance for transfer! Available: $%.2f, Requested: $%.2f\n",
                sender.getBalance(), amount);
            return false;
        }

        // Execute transfer
        sender.debit(amount);
        receiver.credit(amount);

        // Record logs for both parties
        transactions.add(new Transaction("Transfer", amount, sender.getAccountNumber(), "Transferred to Acc: " + receiver.getAccountNumber()));
        transactions.add(new Transaction("Deposit", amount, receiver.getAccountNumber(), "Received from Acc: " + sender.getAccountNumber()));

        System.out.printf("\n[Success] Successfully transferred $%.2f to %s (Acc: %s)!\n",
            amount, receiver.getHolderName(), receiver.getAccountNumber());
        System.out.printf("Sender's Remaining Balance: $%.2f\n", sender.getBalance());
        return true;
    }

    /**
     * Displays mini statement of transactions for a specific account.
     * 
     * @param accountNumber Account number to generate statement for
     */
    public void printMiniStatement(String accountNumber) {
        Account acc = findAccount(accountNumber);
        if (acc == null) {
            System.out.println("[Error] Account not found: " + accountNumber);
            return;
        }

        System.out.println("\n===============================================================================");
        System.out.println("                             MINI STATEMENT                                    ");
        System.out.println("===============================================================================");
        System.out.println("Account Number : " + acc.getAccountNumber());
        System.out.println("Account Holder : " + acc.getHolderName());
        System.out.printf("Current Balance: $%.2f\n", acc.getBalance());
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-19s | %-10s | %-10s | %s\n", "TXN ID", "DATE & TIME", "TYPE", "AMOUNT", "DETAILS");
        System.out.println("-------------------------------------------------------------------------------");

        int count = 0;
        for (Transaction txn : transactions) {
            if (txn.getAccountNumber().equalsIgnoreCase(acc.getAccountNumber())) {
                System.out.println(txn);
                count++;
            }
        }

        if (count == 0) {
            System.out.println("No transaction history recorded for this session.");
        }
        System.out.println("===============================================================================\n");
    }

    /**
     * Retrieves the list of transactions associated with a specific account number.
     * 
     * @param accountNumber Target account number
     * @return List of Transaction objects
     */
    public List<Transaction> getTransactionsForAccount(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        if (accountNumber == null) return list;
        for (Transaction txn : transactions) {
            if (txn.getAccountNumber().equalsIgnoreCase(accountNumber.trim())) {
                list.add(txn);
            }
        }
        return list;
    }
}
