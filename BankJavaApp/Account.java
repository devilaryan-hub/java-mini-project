/**
 * Account.java
 * 
 * Purpose:
 * Represents a single bank account in the Bank Management System.
 * Stores account credentials (account number, holder name, PIN) and maintains balance.
 * Provides standard getters, setters, and helper methods for updating balance.
 * 
 * Target Audience: 2nd-Year Engineering Students learning Core Java & OOP Concepts.
 */

public class Account {
    // Attributes (Encapsulation: private fields with public getters/setters)
    private String accountNumber;
    private String holderName;
    private String pin;
    private double balance;

    /**
     * Parameterized Constructor to initialize a new Account object.
     * 
     * @param accountNumber Unique account identifier (e.g. "1001")
     * @param holderName    Full name of the account holder
     * @param pin           4-digit secret PIN code
     * @param balance       Initial opening balance
     */
    public Account(String accountNumber, String holderName, String pin, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.pin = pin;
        this.balance = balance;
    }

    // ==================== GETTERS AND SETTERS ====================

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Adds an amount to the current balance.
     * 
     * @param amount Positive monetary value to deposit
     */
    public void credit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    /**
     * Deducts an amount from the current balance if funds are sufficient.
     * 
     * @param amount Positive monetary value to withdraw
     * @return true if deduction succeeded, false otherwise
     */
    public boolean debit(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * Validates if the provided PIN matches this account's PIN.
     * 
     * @param enteredPin PIN entered by the user
     * @return true if valid, false otherwise
     */
    public boolean validatePin(String enteredPin) {
        return this.pin != null && this.pin.equals(enteredPin);
    }

    /**
     * String representation of an Account for display purposes.
     */
    @Override
    public String toString() {
        return String.format(
            "----------------------------------------\n" +
            " Account Number : %s\n" +
            " Holder Name    : %s\n" +
            " Current Balance: $%.2f\n" +
            "----------------------------------------",
            accountNumber, holderName, balance
        );
    }
}
