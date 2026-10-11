/**
 * Main.java
 * 
 * Purpose:
 * Entry point and console user interface (CLI) for the Bank Management System.
 * Presents a numbered menu, reads input from user via Scanner, handles exceptions,
 * maintains session state (logged-in user), and coordinates with Bank and FileHandler.
 * 
 * Target Audience: 2nd-Year Engineering Students learning Console I/O and Control Flow.
 */

import java.util.Scanner;

public class Main {
    private static Bank bank;
    private static Scanner scanner;
    private static Account currentSession = null; // Currently logged in user (optional convenience)
    private static final String DATA_FILE = "accounts.txt";

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // 1. Startup: Load existing accounts from accounts.txt
        System.out.println("=================================================");
        System.out.println("       WELCOME TO THE BANK MANAGEMENT SYSTEM     ");
        System.out.println("=================================================");
        bank = new Bank(FileHandler.loadAccounts(DATA_FILE));

        boolean running = true;

        // 2. Main Application Loop
        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-8): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleCreateAccount();
                    break;
                case "2":
                    handleLogin();
                    break;
                case "3":
                    handleDeposit();
                    break;
                case "4":
                    handleWithdraw();
                    break;
                case "5":
                    handleTransfer();
                    break;
                case "6":
                    handleCheckBalance();
                    break;
                case "7":
                    handleMiniStatement();
                    break;
                case "8":
                    // 3. Exit: Persist accounts to file and terminate
                    handleExit();
                    running = false;
                    break;
                default:
                    System.out.println("[Error] Invalid choice! Please enter a number between 1 and 8.\n");
            }
        }

        scanner.close();
    }

    /**
     * Prints the primary console navigation menu.
     */
    private static void printMenu() {
        System.out.println("\n-------------------------------------------------");
        System.out.println("                   MAIN MENU                     ");
        if (currentSession != null) {
            System.out.println(" Active User: " + currentSession.getHolderName() + 
                               " (Acc: " + currentSession.getAccountNumber() + ")");
        } else {
            System.out.println(" Active User: [None - Logged Out]");
        }
        System.out.println("-------------------------------------------------");
        System.out.println(" 1. Create New Account");
        System.out.println(" 2. Login to Account");
        System.out.println(" 3. Deposit Money");
        System.out.println(" 4. Withdraw Money");
        System.out.println(" 5. Transfer Funds");
        System.out.println(" 6. Check Account Balance");
        System.out.println(" 7. Mini Statement (Transaction History)");
        System.out.println(" 8. Exit Application");
        System.out.println("-------------------------------------------------");
    }

    /**
     * Handler for Option 1: Create Account
     */
    private static void handleCreateAccount() {
        System.out.println("\n--- [ CREATE NEW ACCOUNT ] ---");
        try {
            System.out.print("Enter Desired Account Number (e.g. 1001): ");
            String accNo = scanner.nextLine().trim();

            System.out.print("Enter Account Holder Full Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Set a 4-digit Security PIN: ");
            String pin = scanner.nextLine().trim();

            System.out.print("Enter Initial Deposit Amount ($): ");
            double initialDeposit = readDoubleInput();

            boolean success = bank.createAccount(accNo, name, pin, initialDeposit);
            if (success) {
                // Auto-save on account creation
                FileHandler.saveAccounts(bank.getAccounts(), DATA_FILE);
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to create account due to unexpected input: " + e.getMessage());
        }
    }

    /**
     * Handler for Option 2: Login
     */
    private static void handleLogin() {
        System.out.println("\n--- [ ACCOUNT LOGIN ] ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine().trim();

        Account acc = bank.login(accNo, pin);
        if (acc != null) {
            currentSession = acc;
            System.out.println("You are now actively logged in as " + acc.getHolderName() + ".");
        }
    }

    /**
     * Handler for Option 3: Deposit
     */
    private static void handleDeposit() {
        System.out.println("\n--- [ DEPOSIT MONEY ] ---");
        String accNo;

        if (currentSession != null) {
            System.out.println("Using Active Account: " + currentSession.getAccountNumber());
            accNo = currentSession.getAccountNumber();
        } else {
            System.out.print("Enter Target Account Number: ");
            accNo = scanner.nextLine().trim();
        }

        System.out.print("Enter Amount to Deposit ($): ");
        double amount = readDoubleInput();

        boolean success = bank.deposit(accNo, amount);
        if (success) {
            FileHandler.saveAccounts(bank.getAccounts(), DATA_FILE);
        }
    }

    /**
     * Handler for Option 4: Withdraw
     */
    private static void handleWithdraw() {
        System.out.println("\n--- [ WITHDRAW MONEY ] ---");
        String accNo;

        if (currentSession != null) {
            accNo = currentSession.getAccountNumber();
            System.out.println("Using Active Account: " + accNo);
        } else {
            System.out.print("Enter Account Number: ");
            accNo = scanner.nextLine().trim();
        }

        System.out.print("Enter Security PIN: ");
        String pin = scanner.nextLine().trim();

        System.out.print("Enter Amount to Withdraw ($): ");
        double amount = readDoubleInput();

        boolean success = bank.withdraw(accNo, pin, amount);
        if (success) {
            FileHandler.saveAccounts(bank.getAccounts(), DATA_FILE);
        }
    }

    /**
     * Handler for Option 5: Transfer
     */
    private static void handleTransfer() {
        System.out.println("\n--- [ TRANSFER FUNDS ] ---");
        String fromAccNo;

        if (currentSession != null) {
            fromAccNo = currentSession.getAccountNumber();
            System.out.println("Sender Account (Active): " + fromAccNo);
        } else {
            System.out.print("Enter Your (Sender) Account Number: ");
            fromAccNo = scanner.nextLine().trim();
        }

        System.out.print("Enter Sender PIN: ");
        String pin = scanner.nextLine().trim();

        System.out.print("Enter Beneficiary (Receiver) Account Number: ");
        String toAccNo = scanner.nextLine().trim();

        System.out.print("Enter Amount to Transfer ($): ");
        double amount = readDoubleInput();

        boolean success = bank.transfer(fromAccNo, pin, toAccNo, amount);
        if (success) {
            FileHandler.saveAccounts(bank.getAccounts(), DATA_FILE);
        }
    }

    /**
     * Handler for Option 6: Check Balance
     */
    private static void handleCheckBalance() {
        System.out.println("\n--- [ CHECK BALANCE ] ---");
        String accNo;

        if (currentSession != null) {
            accNo = currentSession.getAccountNumber();
        } else {
            System.out.print("Enter Account Number: ");
            accNo = scanner.nextLine().trim();
        }

        System.out.print("Enter Security PIN: ");
        String pin = scanner.nextLine().trim();

        Account acc = bank.findAccount(accNo);
        if (acc == null) {
            System.out.println("[Error] Account number not found: " + accNo);
            return;
        }

        if (!acc.validatePin(pin)) {
            System.out.println("[Error] Incorrect PIN! Access denied.");
            return;
        }

        System.out.println("\n========================================");
        System.out.println(" Account Number: " + acc.getAccountNumber());
        System.out.println(" Holder Name   : " + acc.getHolderName());
        System.out.printf(" Current Balance: $%.2f\n", acc.getBalance());
        System.out.println("========================================");
    }

    /**
     * Handler for Option 7: Mini Statement
     */
    private static void handleMiniStatement() {
        System.out.println("\n--- [ MINI STATEMENT ] ---");
        String accNo;

        if (currentSession != null) {
            accNo = currentSession.getAccountNumber();
        } else {
            System.out.print("Enter Account Number: ");
            accNo = scanner.nextLine().trim();
        }

        System.out.print("Enter Security PIN: ");
        String pin = scanner.nextLine().trim();

        Account acc = bank.findAccount(accNo);
        if (acc == null) {
            System.out.println("[Error] Account not found: " + accNo);
            return;
        }

        if (!acc.validatePin(pin)) {
            System.out.println("[Error] Incorrect PIN! Access denied.");
            return;
        }

        bank.printMiniStatement(accNo);
    }

    /**
     * Handler for Option 8: Exit
     */
    private static void handleExit() {
        System.out.println("\nSaving account data to " + DATA_FILE + "...");
        FileHandler.saveAccounts(bank.getAccounts(), DATA_FILE);
        System.out.println("=================================================");
        System.out.println(" Thank you for banking with us! Have a great day.");
        System.out.println("=================================================");
    }

    /**
     * Helper to reliably read double numeric values without Scanner buffer issues.
     */
    private static double readDoubleInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("[Error] Invalid numeric amount. Please re-enter: ");
            }
        }
    }
}
