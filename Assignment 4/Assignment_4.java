/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lca_02;

/**
 *
 * @author kriii
 */


import java.io.*;
import java.util.*;

// 🔴 Custom Exceptions

class InvalidCIDException extends Exception {
    public InvalidCIDException(String msg) {
        super(msg);
    }
}

class InvalidAmountException extends Exception {
    public InvalidAmountException(String msg) {
        super(msg);
    }
}

class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String msg) {
        super(msg);
    }
}

class DuplicateAccountException extends Exception {
    public DuplicateAccountException(String msg) {
        super(msg);
    }
}

// 🔵 Customer Class

class Customer {
    int cid;
    String cname;
    double amount;

    Customer(int cid, String cname, double amount) {
        this.cid = cid;
        this.cname = cname;
        this.amount = amount;
    }

    void display() {
        System.out.println("CID: " + cid + " | Name: " + cname + " | Balance: " + amount);
    }
}

// 🟢 Main Class

public class Ass4 {

    static Map<Integer, Customer> customers = new HashMap<>();
    static final String FILE_NAME = "customer.txt";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        loadFromFile(); // 🔥 load previous data

        int choice;
        do {
            System.out.println("\n--- Banking Menu ---");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Display All");
            System.out.println("5. Search by CID");
            System.out.println("6. Exit");

            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            try {
                switch (choice) {
                    case 1:
                        createAccount(sc);
                        break;
                    case 2:
                        deposit(sc);
                        break;
                    case 3:
                        withdraw(sc);
                        break;
                    case 4:
                        displayAll();
                        break;
                    case 5:
                        search(sc);
                        break;
                    case 6:
                        saveToFile();
                        System.out.println("Data saved. Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (choice != 6);
    }

    // 🔷 Create Account
    static void createAccount(Scanner sc)
            throws InvalidAmountException, DuplicateAccountException {

        System.out.print("Enter CID: ");
        int cid = sc.nextInt();

        if (customers.containsKey(cid)) {
            throw new DuplicateAccountException("Account with this CID already exists!");
        }

        System.out.print("Enter Name: ");
        String name = sc.next();

        System.out.print("Enter Initial Amount (>=1000): ");
        double amount = sc.nextDouble();

        if (amount < 1000) {
            throw new InvalidAmountException("Minimum balance is 1000");
        }

        customers.put(cid, new Customer(cid, name, amount));
        System.out.println("Account created successfully!");
    }

    // 🔷 Deposit
    static void deposit(Scanner sc) throws InvalidAmountException {

        System.out.print("Enter CID: ");
        int cid = sc.nextInt();

        Customer c = customers.get(cid);

        if (c == null) {
            System.out.println("Account not found!");
            return;
        }

        System.out.print("Enter deposit amount: ");
        double amt = sc.nextDouble();

        if (amt <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }

        c.amount += amt;
        System.out.println("Deposit successful!");
    }

    // 🔷 Withdraw
    static void withdraw(Scanner sc)
            throws InsufficientBalanceException, InvalidAmountException {

        System.out.print("Enter CID: ");
        int cid = sc.nextInt();

        Customer c = customers.get(cid);

        if (c == null) {
            System.out.println("Account not found!");
            return;
        }

        System.out.print("Enter withdrawal amount: ");
        double wth = sc.nextDouble();

        if (wth <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }

        if (wth > c.amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        c.amount -= wth;
        System.out.println("Withdrawal successful!");
    }

    // 🔷 Display All
    static void displayAll() {
        if (customers.isEmpty()) {
            System.out.println("No accounts available!");
            return;
        }

        for (Customer c : customers.values()) {
            c.display();
        }
    }

    // 🔷 Search
    static void search(Scanner sc) {
        System.out.print("Enter CID: ");
        int cid = sc.nextInt();

        Customer c = customers.get(cid);

        if (c != null) {
            c.display();
        } else {
            System.out.println("Account not found!");
        }
    }

    // 🔷 Save to File
    static void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Customer c : customers.values()) {
                bw.write(c.cid + "," + c.cname + "," + c.amount);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("File Error: " + e.getMessage());
        }
    }

    // 🔷 Load from File
    static void loadFromFile() {
    try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
        String line;

        while ((line = br.readLine()) != null) {

            String[] data;

            // 🔥 Handle both formats
            if (line.contains(",")) {
                data = line.split(",");
            } else {
                data = line.split(" ");
            }

            int cid = Integer.parseInt(data[0]);
            String name = data[1];
            double amount = Double.parseDouble(data[2]);

            customers.put(cid, new Customer(cid, name, amount));
        }

        } catch (IOException e) {
        System.out.println("File not found, starting fresh.");
        }
    }
}
