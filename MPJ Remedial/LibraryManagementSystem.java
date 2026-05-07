import java.util.ArrayList;
import java.util.Iterator;

// Book class
class Book {
    private int bookId;
    private String title;
    private String author;
    private String genre;
    private double price;
    private boolean available;

    public Book(int bookId, String title, String author, String genre, double price) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.price = price;
        this.available = true;
    }

    // Getters
    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailable(boolean available) { this.available = available; }

    public void display() {
        System.out.println("ID: " + bookId + " | Title: " + title +
                " | Author: " + author + " | Genre: " + genre +
                " | Price: Rs." + price + " | Available: " + (available ? "Yes" : "No"));
    }
}

// Library Management System
public class LibraryManagementSystem {

    static ArrayList<Book> books = new ArrayList<>();

    // Add a book
    public static void addBook(int id, String title, String author, String genre, double price) {
        books.add(new Book(id, title, author, genre, price));
        System.out.println("Book added: " + title);
    }

    // Display all books
    public static void displayAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }
        System.out.println("\n---------- Book Records ----------");
        for (Book b : books) {
            b.display();
        }
    }

    // Search book by ID
    public static Book searchById(int id) {
        for (Book b : books) {
            if (b.getBookId() == id) return b;
        }
        return null;
    }

    // Search book by title (partial match)
    public static void searchByTitle(String keyword) {
        System.out.println("\nSearch results for: \"" + keyword + "\"");
        boolean found = false;
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                b.display();
                found = true;
            }
        }
        if (!found) System.out.println("No book found with title containing \"" + keyword + "\"");
    }

    // Update book by ID
    public static void updateBook(int id, String newTitle, String newAuthor, double newPrice) {
        Book b = searchById(id);
        if (b != null) {
            b.setTitle(newTitle);
            b.setAuthor(newAuthor);
            b.setPrice(newPrice);
            System.out.println("Book ID " + id + " updated successfully.");
        } else {
            System.out.println("Book ID " + id + " not found.");
        }
    }

    // Delete book by ID
    public static void deleteBook(int id) {
        Iterator<Book> iter = books.iterator();
        while (iter.hasNext()) {
            Book b = iter.next();
            if (b.getBookId() == id) {
                iter.remove();
                System.out.println("Book ID " + id + " deleted successfully.");
                return;
            }
        }
        System.out.println("Book ID " + id + " not found.");
    }

    public static void main(String[] args) {
        System.out.println("===== LIBRARY MANAGEMENT SYSTEM =====\n");

        // Add books
        addBook(101, "The Alchemist", "Paulo Coelho", "Fiction", 350.0);
        addBook(102, "Clean Code", "Robert C. Martin", "Technology", 650.0);
        addBook(103, "Wings of Fire", "APJ Abdul Kalam", "Biography", 299.0);
        addBook(104, "Atomic Habits", "James Clear", "Self-Help", 450.0);
        addBook(105, "Deep Work", "Cal Newport", "Self-Help", 399.0);

        // Display all
        displayAllBooks();

        // Search by title
        System.out.println("\n--- Searching for 'habit' ---");
        searchByTitle("habit");

        // Update a book
        System.out.println("\n--- Updating Book ID 102 ---");
        updateBook(102, "Clean Code (2nd Ed)", "Robert C. Martin", 750.0);

        // Delete a book
        System.out.println("\n--- Deleting Book ID 103 ---");
        deleteBook(103);

        // Display after changes
        System.out.println("\n--- Books After Update and Delete ---");
        displayAllBooks();

        System.out.println("\nTotal Books in Library: " + books.size());
    }
}
