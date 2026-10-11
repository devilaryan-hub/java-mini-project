/**
 * FileHandler.java
 * 
 * Purpose:
 * Provides file persistence capabilities for the Bank Management System.
 * Reads and writes account records to a simple comma-separated text file (accounts.txt).
 * This ensures customer accounts persist even after closing and restarting the application.
 * 
 * Target Audience: 2nd-Year Engineering Students learning File I/O in Java.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    // Default file name used for saving accounts
    private static final String DEFAULT_FILE_NAME = "accounts.txt";

    /**
     * Saves all accounts to a comma-separated text file.
     * Format per line: accountNumber,holderName,pin,balance
     * 
     * @param accounts List of Account objects to persist
     * @param filePath Path or filename where data should be saved
     * @return true if save was successful, false otherwise
     */
    public static boolean saveAccounts(List<Account> accounts, String filePath) {
        String path = (filePath != null && !filePath.trim().isEmpty()) ? filePath : DEFAULT_FILE_NAME;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (Account acc : accounts) {
                // Write line: 1001,John Doe,1234,1500.00
                String line = String.format("%s,%s,%s,%.2f",
                    acc.getAccountNumber(),
                    acc.getHolderName(),
                    acc.getPin(),
                    acc.getBalance()
                );
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("[FileHandler Error] Failed to save accounts to file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves accounts using default file path ("accounts.txt").
     */
    public static boolean saveAccounts(List<Account> accounts) {
        return saveAccounts(accounts, DEFAULT_FILE_NAME);
    }

    /**
     * Reads accounts from a comma-separated text file on startup.
     * If the file does not exist yet, returns an empty list without throwing an error.
     * 
     * @param filePath Path or filename to load from
     * @return List of parsed Account objects
     */
    public static ArrayList<Account> loadAccounts(String filePath) {
        ArrayList<Account> accounts = new ArrayList<>();
        String path = (filePath != null && !filePath.trim().isEmpty()) ? filePath : DEFAULT_FILE_NAME;
        File file = new File(path);

        // If file doesn't exist yet (first run), return empty list
        if (!file.exists()) {
            System.out.println("[FileHandler Info] '" + path + "' not found. Initializing with empty account store.");
            return accounts;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip blank lines and comments
                }

                // Split by comma
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    try {
                        String accNo = parts[0].trim();
                        String name = parts[1].trim();
                        String pin = parts[2].trim();
                        double balance = Double.parseDouble(parts[3].trim());

                        accounts.add(new Account(accNo, name, pin, balance));
                    } catch (NumberFormatException e) {
                        System.err.println("[FileHandler Warning] Skipping corrupted line " + lineNumber + ": " + line);
                    }
                } else {
                    System.err.println("[FileHandler Warning] Invalid data format on line " + lineNumber + ": " + line);
                }
            }
            System.out.println("[FileHandler Info] Successfully loaded " + accounts.size() + " account(s) from " + path);
        } catch (IOException e) {
            System.err.println("[FileHandler Error] Error reading accounts file: " + e.getMessage());
        }

        return accounts;
    }

    /**
     * Loads accounts using default file path ("accounts.txt").
     */
    public static ArrayList<Account> loadAccounts() {
        return loadAccounts(DEFAULT_FILE_NAME);
    }
}
