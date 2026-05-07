import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Product class
class Product {
    private int productId;
    private String name;
    private String category;
    private double price;
    private int quantity;

    public Product(int productId, String name, String category, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void display() {
        System.out.printf("ID: %-5d | Name: %-20s | Category: %-15s | Price: Rs.%-8.2f | Qty: %d%n",
                productId, name, category, price, quantity);
    }
}

// Inventory Management System
public class InventoryManagementSystem {

    static ArrayList<Product> inventory = new ArrayList<>();

    // Add product
    public static void addProduct(int id, String name, String category, double price, int qty) {
        // Check if ID already exists
        for (Product p : inventory) {
            if (p.getProductId() == id) {
                System.out.println("Product ID " + id + " already exists. Use update instead.");
                return;
            }
        }
        inventory.add(new Product(id, name, category, price, qty));
        System.out.println("Product added: " + name);
    }

    // Display all products
    public static void displayAll() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                        INVENTORY RECORDS");
        System.out.println("=".repeat(80));
        System.out.printf("%-6s | %-20s | %-15s | %-12s | %s%n",
                "ID", "Name", "Category", "Price", "Quantity");
        System.out.println("-".repeat(80));
        for (Product p : inventory) {
            p.display();
        }
        System.out.println("=".repeat(80));
    }

    // Search by ID
    public static Product searchById(int id) {
        for (Product p : inventory) {
            if (p.getProductId() == id) return p;
        }
        return null;
    }

    // Search by name
    public static void searchByName(String keyword) {
        System.out.println("\nSearch results for: \"" + keyword + "\"");
        boolean found = false;
        for (Product p : inventory) {
            if (p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                p.display();
                found = true;
            }
        }
        if (!found) System.out.println("No product found with name containing \"" + keyword + "\"");
    }

    // Update product
    public static void updateProduct(int id, String newName, String newCategory, double newPrice, int newQty) {
        Product p = searchById(id);
        if (p != null) {
            p.setName(newName);
            p.setCategory(newCategory);
            p.setPrice(newPrice);
            p.setQuantity(newQty);
            System.out.println("Product ID " + id + " updated successfully.");
        } else {
            System.out.println("Product ID " + id + " not found.");
        }
    }

    // Delete product
    public static void deleteProduct(int id) {
        Product toDelete = searchById(id);
        if (toDelete != null) {
            inventory.remove(toDelete);
            System.out.println("Product ID " + id + " (" + toDelete.getName() + ") deleted.");
        } else {
            System.out.println("Product ID " + id + " not found.");
        }
    }

    // Display category-wise summary
    public static void categorySummary() {
        Map<String, Integer> summary = new HashMap<>();
        for (Product p : inventory) {
            summary.put(p.getCategory(), summary.getOrDefault(p.getCategory(), 0) + p.getQuantity());
        }
        System.out.println("\n--- Category-wise Stock Summary ---");
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            System.out.println("Category: " + entry.getKey() + " | Total Stock: " + entry.getValue());
        }
    }

    // Display low stock (qty < threshold)
    public static void lowStockAlert(int threshold) {
        System.out.println("\n--- Low Stock Alert (Qty < " + threshold + ") ---");
        boolean found = false;
        for (Product p : inventory) {
            if (p.getQuantity() < threshold) {
                p.display();
                found = true;
            }
        }
        if (!found) System.out.println("All products have sufficient stock.");
    }

    public static void main(String[] args) {
        System.out.println("===== INVENTORY MANAGEMENT SYSTEM =====");

        // Add products
        addProduct(1001, "Laptop", "Electronics", 55000.0, 15);
        addProduct(1002, "Wireless Mouse", "Electronics", 899.0, 50);
        addProduct(1003, "Notebook (200 pages)", "Stationery", 45.0, 200);
        addProduct(1004, "Office Chair", "Furniture", 8500.0, 8);
        addProduct(1005, "Pen Drive 64GB", "Electronics", 499.0, 4);
        addProduct(1006, "Whiteboard Marker", "Stationery", 25.0, 3);
        addProduct(1007, "Standing Desk", "Furniture", 15000.0, 5);

        // Display all
        displayAll();

        // Search by name
        System.out.println("\n--- Searching for 'electronic' products ---");
        searchByName("Mouse");

        // Update a product
        System.out.println("\n--- Updating Product ID 1001 ---");
        updateProduct(1001, "Gaming Laptop", "Electronics", 75000.0, 10);

        // Delete a product
        System.out.println("\n--- Deleting Product ID 1003 ---");
        deleteProduct(1003);

        // Display updated inventory
        displayAll();

        // Category summary
        categorySummary();

        // Low stock alert
        lowStockAlert(10);

        System.out.println("\nTotal Products in Inventory: " + inventory.size());
    }
}
