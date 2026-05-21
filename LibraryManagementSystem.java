import java.util.*;
import java.time.*;
import java.time.format.*;
import java.io.*;

// ==================== BOOK CLASS ====================
class Book implements Serializable {
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private int totalCopies;
    private int availableCopies;
    private int yearPublished;

    public Book(String bookId, String title, String author, String genre, int totalCopies, int yearPublished) {
        this.bookId        = bookId;
        this.title         = title;
        this.author        = author;
        this.genre         = genre;
        this.totalCopies   = totalCopies;
        this.availableCopies = totalCopies;
        this.yearPublished = yearPublished;
    }

    // Getters
    public String getBookId()         { return bookId; }
    public String getTitle()          { return title; }
    public String getAuthor()         { return author; }
    public String getGenre()          { return genre; }
    public int    getTotalCopies()    { return totalCopies; }
    public int    getAvailableCopies(){ return availableCopies; }
    public int    getYearPublished()  { return yearPublished; }

    // Setters
    public void setTitle(String title)           { this.title = title; }
    public void setAuthor(String author)         { this.author = author; }
    public void setGenre(String genre)           { this.genre = genre; }
    public void setTotalCopies(int n)            { this.totalCopies = n; }
    public void setAvailableCopies(int n)        { this.availableCopies = n; }
    public void setYearPublished(int y)          { this.yearPublished = y; }

    public boolean isAvailable() { return availableCopies > 0; }

    public void borrowCopy()  { if (availableCopies > 0) availableCopies--; }
    public void returnCopy()  { if (availableCopies < totalCopies) availableCopies++; }

    @Override
    public String toString() {
        return String.format("%-10s %-35s %-22s %-15s %5d/%d   %d",
                bookId, title, author, genre, availableCopies, totalCopies, yearPublished);
    }
}

// ==================== MEMBER CLASS ====================
class Member implements Serializable {
    private String memberId;
    private String name;
    private String email;
    private String phone;
    private String membershipType; // Student, Faculty, General
    private LocalDate joinDate;
    private boolean isActive;

    public Member(String memberId, String name, String email, String phone, String membershipType) {
        this.memberId       = memberId;
        this.name           = name;
        this.email          = email;
        this.phone          = phone;
        this.membershipType = membershipType;
        this.joinDate       = LocalDate.now();
        this.isActive       = true;
    }

    public String    getMemberId()       { return memberId; }
    public String    getName()           { return name; }
    public String    getEmail()          { return email; }
    public String    getPhone()          { return phone; }
    public String    getMembershipType() { return membershipType; }
    public LocalDate getJoinDate()       { return joinDate; }
    public boolean   isActive()          { return isActive; }

    public void setName(String name)               { this.name = name; }
    public void setEmail(String email)             { this.email = email; }
    public void setPhone(String phone)             { this.phone = phone; }
    public void setMembershipType(String type)     { this.membershipType = type; }
    public void setActive(boolean active)          { this.isActive = active; }

    @Override
    public String toString() {
        return String.format("%-10s %-22s %-28s %-14s %-10s %-12s %s",
                memberId, name, email, phone, membershipType,
                joinDate.toString(), isActive ? "Active" : "Inactive");
    }
}

// ==================== TRANSACTION CLASS ====================
class Transaction implements Serializable {
    private String      transactionId;
    private String      memberId;
    private String      bookId;
    private LocalDate   issueDate;
    private LocalDate   dueDate;
    private LocalDate   returnDate;
    private boolean     isReturned;
    private double      fineAmount;
    private static final int MAX_BORROW_DAYS = 14;
    private static final double FINE_PER_DAY  = 2.0;

    public Transaction(String transactionId, String memberId, String bookId) {
        this.transactionId = transactionId;
        this.memberId      = memberId;
        this.bookId        = bookId;
        this.issueDate     = LocalDate.now();
        this.dueDate       = LocalDate.now().plusDays(MAX_BORROW_DAYS);
        this.isReturned    = false;
        this.fineAmount    = 0.0;
    }

    public String    getTransactionId() { return transactionId; }
    public String    getMemberId()       { return memberId; }
    public String    getBookId()         { return bookId; }
    public LocalDate getIssueDate()      { return issueDate; }
    public LocalDate getDueDate()        { return dueDate; }
    public LocalDate getReturnDate()     { return returnDate; }
    public boolean   isReturned()        { return isReturned; }
    public double    getFineAmount()     { return fineAmount; }

    public void returnBook() {
        this.returnDate = LocalDate.now();
        this.isReturned = true;
        if (returnDate.isAfter(dueDate)) {
            long daysLate = dueDate.until(returnDate).getDays();
            this.fineAmount = daysLate * FINE_PER_DAY;
        }
    }

    public double calculateCurrentFine() {
        if (isReturned) return fineAmount;
        LocalDate today = LocalDate.now();
        if (today.isAfter(dueDate)) {
            long daysLate = dueDate.until(today).getDays();
            return daysLate * FINE_PER_DAY;
        }
        return 0.0;
    }

    @Override
    public String toString() {
        String ret = isReturned ? (returnDate != null ? returnDate.toString() : "N/A") : "Not Returned";
        return String.format("%-14s %-10s %-10s %-12s %-12s %-14s Rs.%.2f",
                transactionId, memberId, bookId,
                issueDate.toString(), dueDate.toString(), ret, calculateCurrentFine());
    }
}

// ==================== LIBRARY CLASS ====================
class Library {
    private Map<String, Book>        books        = new LinkedHashMap<>();
    private Map<String, Member>      members      = new LinkedHashMap<>();
    private Map<String, Transaction> transactions = new LinkedHashMap<>();
    private int bookCounter   = 1;
    private int memberCounter = 1;
    private int txnCounter    = 1;

    // -------- ID Generators --------
    private String generateBookId()   { return String.format("BK%04d", bookCounter++); }
    private String generateMemberId() { return String.format("MB%04d", memberCounter++); }
    private String generateTxnId()    { return String.format("TX%05d", txnCounter++); }

    // ======== BOOK OPERATIONS ========

    public String addBook(String title, String author, String genre, int copies, int year) {
        String id = generateBookId();
        books.put(id, new Book(id, title, author, genre, copies, year));
        return id;
    }

    public boolean removeBook(String bookId) {
        if (!books.containsKey(bookId)) return false;
        // Check if any copy is currently borrowed
        boolean hasPending = transactions.values().stream()
                .anyMatch(t -> t.getBookId().equals(bookId) && !t.isReturned());
        if (hasPending) return false;
        books.remove(bookId);
        return true;
    }

    public Book findBookById(String bookId) { return books.get(bookId); }

    public List<Book> searchBooks(String keyword) {
        String kw = keyword.toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book b : books.values()) {
            if (b.getTitle().toLowerCase().contains(kw)
                    || b.getAuthor().toLowerCase().contains(kw)
                    || b.getGenre().toLowerCase().contains(kw)
                    || b.getBookId().toLowerCase().contains(kw)) {
                result.add(b);
            }
        }
        return result;
    }

    public Collection<Book> getAllBooks() { return books.values(); }

    public boolean updateBook(String bookId, String title, String author, String genre, int copies, int year) {
        Book b = books.get(bookId);
        if (b == null) return false;
        int borrowed = b.getTotalCopies() - b.getAvailableCopies();
        if (copies < borrowed) return false; // can't reduce below currently borrowed count
        b.setTitle(title);
        b.setAuthor(author);
        b.setGenre(genre);
        b.setAvailableCopies(copies - borrowed);
        b.setTotalCopies(copies);
        b.setYearPublished(year);
        return true;
    }

    // ======== MEMBER OPERATIONS ========

    public String addMember(String name, String email, String phone, String type) {
        String id = generateMemberId();
        members.put(id, new Member(id, name, email, phone, type));
        return id;
    }

    public boolean removeMember(String memberId) {
        if (!members.containsKey(memberId)) return false;
        boolean hasPending = transactions.values().stream()
                .anyMatch(t -> t.getMemberId().equals(memberId) && !t.isReturned());
        if (hasPending) return false;
        members.remove(memberId);
        return true;
    }

    public Member findMemberById(String memberId) { return members.get(memberId); }

    public List<Member> searchMembers(String keyword) {
        String kw = keyword.toLowerCase();
        List<Member> result = new ArrayList<>();
        for (Member m : members.values()) {
            if (m.getName().toLowerCase().contains(kw)
                    || m.getEmail().toLowerCase().contains(kw)
                    || m.getMemberId().toLowerCase().contains(kw)) {
                result.add(m);
            }
        }
        return result;
    }

    public Collection<Member> getAllMembers() { return members.values(); }

    public boolean updateMember(String memberId, String name, String email, String phone, String type) {
        Member m = members.get(memberId);
        if (m == null) return false;
        m.setName(name);
        m.setEmail(email);
        m.setPhone(phone);
        m.setMembershipType(type);
        return true;
    }

    // ======== BORROW / RETURN ========

    public String borrowBook(String memberId, String bookId) {
        Member m = members.get(memberId);
        Book   b = books.get(bookId);
        if (m == null)         return "ERROR: Member not found.";
        if (!m.isActive())     return "ERROR: Member account is inactive.";
        if (b == null)         return "ERROR: Book not found.";
        if (!b.isAvailable())  return "ERROR: No copies available right now.";

        // Check borrow limit (max 3 books per member)
        long currentBorrows = transactions.values().stream()
                .filter(t -> t.getMemberId().equals(memberId) && !t.isReturned())
                .count();
        if (currentBorrows >= 3) return "ERROR: Borrow limit reached (max 3 books).";

        // Check if same book already borrowed
        boolean alreadyBorrowed = transactions.values().stream()
                .anyMatch(t -> t.getMemberId().equals(memberId)
                        && t.getBookId().equals(bookId) && !t.isReturned());
        if (alreadyBorrowed) return "ERROR: Member already has this book.";

        String txnId = generateTxnId();
        Transaction txn = new Transaction(txnId, memberId, bookId);
        transactions.put(txnId, txn);
        b.borrowCopy();
        return "SUCCESS: Book issued. Transaction ID: " + txnId
                + " | Due Date: " + txn.getDueDate();
    }

    public String returnBook(String transactionId) {
        Transaction txn = transactions.get(transactionId);
        if (txn == null)       return "ERROR: Transaction not found.";
        if (txn.isReturned())  return "ERROR: Book already returned.";

        txn.returnBook();
        Book b = books.get(txn.getBookId());
        if (b != null) b.returnCopy();

        String msg = "SUCCESS: Book returned.";
        if (txn.getFineAmount() > 0)
            msg += " Fine charged: Rs." + String.format("%.2f", txn.getFineAmount());
        return msg;
    }

    // ======== TRANSACTION QUERIES ========

    public Collection<Transaction> getAllTransactions() { return transactions.values(); }

    public List<Transaction> getMemberTransactions(String memberId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions.values())
            if (t.getMemberId().equals(memberId)) result.add(t);
        return result;
    }

    public List<Transaction> getBookTransactions(String bookId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions.values())
            if (t.getBookId().equals(bookId)) result.add(t);
        return result;
    }

    public List<Transaction> getOverdueTransactions() {
        LocalDate today = LocalDate.now();
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions.values())
            if (!t.isReturned() && today.isAfter(t.getDueDate())) result.add(t);
        return result;
    }

    public List<Transaction> getActiveTransactions() {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions.values())
            if (!t.isReturned()) result.add(t);
        return result;
    }

    // ======== STATISTICS ========

    public int  totalBooks()          { return books.size(); }
    public int  totalMembers()        { return members.size(); }
    public int  totalTransactions()   { return transactions.size(); }
    public int  activeTransactions()  { return (int) transactions.values().stream().filter(t -> !t.isReturned()).count(); }
    public int  overdueCount()        { return getOverdueTransactions().size(); }
    public double totalFinesCollected() {
        return transactions.values().stream()
                .filter(Transaction::isReturned)
                .mapToDouble(Transaction::getFineAmount)
                .sum();
    }
    public double pendingFines() {
        return transactions.values().stream()
                .filter(t -> !t.isReturned())
                .mapToDouble(Transaction::calculateCurrentFine)
                .sum();
    }
}

// ==================== MAIN APPLICATION ====================
public class LibraryManagementSystem {

    private static final Library lib     = new Library();
    private static final Scanner scanner = new Scanner(System.in);

    // ─── ANSI colours (work on most terminals) ───
    static final String RESET  = "\u001B[0m";
    static final String BOLD   = "\u001B[1m";
    static final String CYAN   = "\u001B[36m";
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE   = "\u001B[34m";
    static final String PURPLE = "\u001B[35m";

    public static void main(String[] args) {
        loadSampleData();
        printBanner();
        mainMenu();
    }

    // ─── SAMPLE DATA ───────────────────────────────────────────
    static void loadSampleData() {
        // Books
        lib.addBook("The Great Gatsby",           "F. Scott Fitzgerald", "Classic",  3, 1925);
        lib.addBook("To Kill a Mockingbird",       "Harper Lee",          "Fiction",  2, 1960);
        lib.addBook("1984",                        "George Orwell",       "Dystopian",4, 1949);
        lib.addBook("Clean Code",                  "Robert C. Martin",    "Technology",3, 2008);
        lib.addBook("Design Patterns",             "Gang of Four",        "Technology",2, 1994);
        lib.addBook("The Alchemist",               "Paulo Coelho",        "Fiction",  5, 1988);
        lib.addBook("Sapiens",                     "Yuval Noah Harari",   "History",  3, 2011);
        lib.addBook("Introduction to Algorithms",  "CLRS",                "Technology",2, 2009);
        lib.addBook("Pride and Prejudice",         "Jane Austen",         "Classic",  3, 1813);
        lib.addBook("Harry Potter & Sorcerer's Stone","J.K. Rowling",     "Fantasy",  4, 1997);

        // Members
        lib.addMember("Aarav Sharma",   "aarav@email.com",   "9876543210", "Student");
        lib.addMember("Priya Patel",    "priya@email.com",   "9876543211", "Faculty");
        lib.addMember("Rahul Verma",    "rahul@email.com",   "9876543212", "General");
        lib.addMember("Sneha Joshi",    "sneha@email.com",   "9876543213", "Student");
        lib.addMember("Amit Gupta",     "amit@email.com",    "9876543214", "Faculty");
    }

    // ─── BANNER ─────────────────────────────────────────────────
    static void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║        📚  LIBRARY MANAGEMENT SYSTEM  📚                ║");
        System.out.println("║              Console-Based PBL Project                  ║");
        System.out.println("║                  Java Edition                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    // ─── MAIN MENU ──────────────────────────────────────────────
    static void mainMenu() {
        while (true) {
            System.out.println(BOLD + BLUE + "\n══════════════ MAIN MENU ══════════════" + RESET);
            System.out.println("  1. 📖  Book Management");
            System.out.println("  2. 👤  Member Management");
            System.out.println("  3. 🔄  Issue / Return Book");
            System.out.println("  4. 📋  Transactions & Reports");
            System.out.println("  5. 📊  Dashboard / Statistics");
            System.out.println("  0. 🚪  Exit");
            System.out.println(BLUE + "══════════════════════════════════════" + RESET);
            int choice = getIntInput("Enter choice: ");
            switch (choice) {
                case 1 -> bookMenu();
                case 2 -> memberMenu();
                case 3 -> issueReturnMenu();
                case 4 -> transactionMenu();
                case 5 -> showDashboard();
                case 0 -> { System.out.println(GREEN + "\nGoodbye! Happy Reading! 📚" + RESET); return; }
                default -> System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }

    // ─── BOOK MENU ──────────────────────────────────────────────
    static void bookMenu() {
        while (true) {
            System.out.println(BOLD + PURPLE + "\n────── BOOK MANAGEMENT ──────" + RESET);
            System.out.println("  1. Add New Book");
            System.out.println("  2. View All Books");
            System.out.println("  3. Search Books");
            System.out.println("  4. Update Book");
            System.out.println("  5. Remove Book");
            System.out.println("  6. View Book Details");
            System.out.println("  0. Back");
            int choice = getIntInput("Choice: ");
            switch (choice) {
                case 1 -> addBook();
                case 2 -> viewAllBooks();
                case 3 -> searchBooks();
                case 4 -> updateBook();
                case 5 -> removeBook();
                case 6 -> viewBookDetails();
                case 0 -> { return; }
                default -> System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }

    static void addBook() {
        System.out.println(CYAN + "\n--- Add New Book ---" + RESET);
        String title  = getStringInput("Title       : ");
        String author = getStringInput("Author      : ");
        String genre  = getStringInput("Genre       : ");
        int    copies = getIntInput(   "Copies      : ");
        int    year   = getIntInput(   "Pub. Year   : ");
        String id = lib.addBook(title, author, genre, copies, year);
        System.out.println(GREEN + "✔ Book added! ID: " + id + RESET);
    }

    static void viewAllBooks() {
        Collection<Book> books = lib.getAllBooks();
        if (books.isEmpty()) { System.out.println(YELLOW + "No books in library." + RESET); return; }
        printBookHeader();
        for (Book b : books) System.out.println(b);
        System.out.println(CYAN + "Total: " + books.size() + " book(s)" + RESET);
    }

    static void searchBooks() {
        String kw = getStringInput("Search (title/author/genre/ID): ");
        List<Book> results = lib.searchBooks(kw);
        if (results.isEmpty()) { System.out.println(YELLOW + "No books found." + RESET); return; }
        printBookHeader();
        results.forEach(System.out::println);
        System.out.println(CYAN + results.size() + " result(s)" + RESET);
    }

    static void updateBook() {
        String id = getStringInput("Book ID to update: ");
        Book b = lib.findBookById(id);
        if (b == null) { System.out.println(RED + "Book not found." + RESET); return; }
        System.out.println("Leave blank to keep current value.");
        String title  = getStringInputOptional("Title  [" + b.getTitle()  + "]: ", b.getTitle());
        String author = getStringInputOptional("Author [" + b.getAuthor() + "]: ", b.getAuthor());
        String genre  = getStringInputOptional("Genre  [" + b.getGenre()  + "]: ", b.getGenre());
        int    copies = getIntInputOptional("Copies [" + b.getTotalCopies() + "]: ", b.getTotalCopies());
        int    year   = getIntInputOptional("Year   [" + b.getYearPublished() + "]: ", b.getYearPublished());
        if (lib.updateBook(id, title, author, genre, copies, year))
            System.out.println(GREEN + "✔ Book updated." + RESET);
        else
            System.out.println(RED + "Update failed (copies cannot be less than currently borrowed)." + RESET);
    }

    static void removeBook() {
        String id = getStringInput("Book ID to remove: ");
        if (lib.removeBook(id))
            System.out.println(GREEN + "✔ Book removed." + RESET);
        else
            System.out.println(RED + "Cannot remove: book not found or has pending borrows." + RESET);
    }

    static void viewBookDetails() {
        String id = getStringInput("Book ID: ");
        Book b = lib.findBookById(id);
        if (b == null) { System.out.println(RED + "Book not found." + RESET); return; }
        System.out.println(CYAN + "\n══ Book Details ══" + RESET);
        System.out.println("  ID        : " + b.getBookId());
        System.out.println("  Title     : " + b.getTitle());
        System.out.println("  Author    : " + b.getAuthor());
        System.out.println("  Genre     : " + b.getGenre());
        System.out.println("  Year      : " + b.getYearPublished());
        System.out.println("  Copies    : " + b.getAvailableCopies() + " available / " + b.getTotalCopies() + " total");
        System.out.println("  Status    : " + (b.isAvailable() ? GREEN + "Available" : RED + "All Borrowed") + RESET);
        List<Transaction> hist = lib.getBookTransactions(id);
        System.out.println("  Borrow History: " + hist.size() + " time(s)");
    }

    // ─── MEMBER MENU ────────────────────────────────────────────
    static void memberMenu() {
        while (true) {
            System.out.println(BOLD + PURPLE + "\n────── MEMBER MANAGEMENT ──────" + RESET);
            System.out.println("  1. Register Member");
            System.out.println("  2. View All Members");
            System.out.println("  3. Search Members");
            System.out.println("  4. Update Member");
            System.out.println("  5. Deactivate / Activate Member");
            System.out.println("  6. View Member Details");
            System.out.println("  7. Remove Member");
            System.out.println("  0. Back");
            int choice = getIntInput("Choice: ");
            switch (choice) {
                case 1 -> addMember();
                case 2 -> viewAllMembers();
                case 3 -> searchMembers();
                case 4 -> updateMember();
                case 5 -> toggleMemberStatus();
                case 6 -> viewMemberDetails();
                case 7 -> removeMember();
                case 0 -> { return; }
                default -> System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }

    static void addMember() {
        System.out.println(CYAN + "\n--- Register Member ---" + RESET);
        String name  = getStringInput("Name   : ");
        String email = getStringInput("Email  : ");
        String phone = getStringInput("Phone  : ");
        System.out.println("Type: 1.Student  2.Faculty  3.General");
        int t = getIntInput("Type   : ");
        String type = switch (t) { case 1 -> "Student"; case 2 -> "Faculty"; default -> "General"; };
        String id = lib.addMember(name, email, phone, type);
        System.out.println(GREEN + "✔ Member registered! ID: " + id + RESET);
    }

    static void viewAllMembers() {
        Collection<Member> members = lib.getAllMembers();
        if (members.isEmpty()) { System.out.println(YELLOW + "No members registered." + RESET); return; }
        printMemberHeader();
        for (Member m : members) System.out.println(m);
        System.out.println(CYAN + "Total: " + members.size() + " member(s)" + RESET);
    }

    static void searchMembers() {
        String kw = getStringInput("Search (name/email/ID): ");
        List<Member> results = lib.searchMembers(kw);
        if (results.isEmpty()) { System.out.println(YELLOW + "No members found." + RESET); return; }
        printMemberHeader();
        results.forEach(System.out::println);
    }

    static void updateMember() {
        String id = getStringInput("Member ID to update: ");
        Member m = lib.findMemberById(id);
        if (m == null) { System.out.println(RED + "Member not found." + RESET); return; }
        System.out.println("Leave blank to keep current value.");
        String name  = getStringInputOptional("Name  [" + m.getName()  + "]: ", m.getName());
        String email = getStringInputOptional("Email [" + m.getEmail() + "]: ", m.getEmail());
        String phone = getStringInputOptional("Phone [" + m.getPhone() + "]: ", m.getPhone());
        String type  = getStringInputOptional("Type  [" + m.getMembershipType() + "]: ", m.getMembershipType());
        if (lib.updateMember(id, name, email, phone, type))
            System.out.println(GREEN + "✔ Member updated." + RESET);
        else
            System.out.println(RED + "Update failed." + RESET);
    }

    static void toggleMemberStatus() {
        String id = getStringInput("Member ID: ");
        Member m = lib.findMemberById(id);
        if (m == null) { System.out.println(RED + "Member not found." + RESET); return; }
        m.setActive(!m.isActive());
        System.out.println(GREEN + "✔ Member is now " + (m.isActive() ? "Active" : "Inactive") + RESET);
    }

    static void viewMemberDetails() {
        String id = getStringInput("Member ID: ");
        Member m = lib.findMemberById(id);
        if (m == null) { System.out.println(RED + "Member not found." + RESET); return; }
        System.out.println(CYAN + "\n══ Member Details ══" + RESET);
        System.out.println("  ID         : " + m.getMemberId());
        System.out.println("  Name       : " + m.getName());
        System.out.println("  Email      : " + m.getEmail());
        System.out.println("  Phone      : " + m.getPhone());
        System.out.println("  Type       : " + m.getMembershipType());
        System.out.println("  Join Date  : " + m.getJoinDate());
        System.out.println("  Status     : " + (m.isActive() ? GREEN + "Active" : RED + "Inactive") + RESET);

        List<Transaction> txns = lib.getMemberTransactions(id);
        long active = txns.stream().filter(t -> !t.isReturned()).count();
        System.out.println("  Borrowed   : " + active + " book(s) currently");
        double pending = txns.stream().filter(t -> !t.isReturned()).mapToDouble(Transaction::calculateCurrentFine).sum();
        if (pending > 0) System.out.println("  Pending Fine: " + RED + "Rs." + String.format("%.2f", pending) + RESET);

        if (!txns.isEmpty()) {
            System.out.println(CYAN + "\n  Transaction History:" + RESET);
            printTransactionHeader();
            txns.forEach(System.out::println);
        }
    }

    static void removeMember() {
        String id = getStringInput("Member ID to remove: ");
        if (lib.removeMember(id))
            System.out.println(GREEN + "✔ Member removed." + RESET);
        else
            System.out.println(RED + "Cannot remove: member not found or has pending borrows." + RESET);
    }

    // ─── ISSUE / RETURN MENU ────────────────────────────────────
    static void issueReturnMenu() {
        while (true) {
            System.out.println(BOLD + PURPLE + "\n────── ISSUE / RETURN ──────" + RESET);
            System.out.println("  1. Issue Book to Member");
            System.out.println("  2. Return Book");
            System.out.println("  3. Check Current Fine for Transaction");
            System.out.println("  0. Back");
            int choice = getIntInput("Choice: ");
            switch (choice) {
                case 1 -> issueBook();
                case 2 -> returnBook();
                case 3 -> checkFine();
                case 0 -> { return; }
                default -> System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }

    static void issueBook() {
        System.out.println(CYAN + "\n--- Issue Book ---" + RESET);
        String memberId = getStringInput("Member ID : ");
        String bookId   = getStringInput("Book ID   : ");
        String result = lib.borrowBook(memberId, bookId);
        if (result.startsWith("SUCCESS"))
            System.out.println(GREEN + result + RESET);
        else
            System.out.println(RED + result + RESET);
    }

    static void returnBook() {
        System.out.println(CYAN + "\n--- Return Book ---" + RESET);
        String txnId = getStringInput("Transaction ID: ");
        String result = lib.returnBook(txnId);
        if (result.startsWith("SUCCESS"))
            System.out.println(GREEN + result + RESET);
        else
            System.out.println(RED + result + RESET);
    }

    static void checkFine() {
        String txnId = getStringInput("Transaction ID: ");
        for (Transaction t : lib.getAllTransactions()) {
            if (t.getTransactionId().equals(txnId)) {
                double fine = t.calculateCurrentFine();
                System.out.println("Fine for " + txnId + ": " + (fine > 0
                        ? RED + "Rs." + String.format("%.2f", fine) + RESET
                        : GREEN + "No fine" + RESET));
                return;
            }
        }
        System.out.println(RED + "Transaction not found." + RESET);
    }

    // ─── TRANSACTION MENU ───────────────────────────────────────
    static void transactionMenu() {
        while (true) {
            System.out.println(BOLD + PURPLE + "\n────── TRANSACTIONS & REPORTS ──────" + RESET);
            System.out.println("  1. All Transactions");
            System.out.println("  2. Active Borrows (not returned)");
            System.out.println("  3. Overdue Books");
            System.out.println("  4. Transactions by Member");
            System.out.println("  5. Transactions by Book");
            System.out.println("  0. Back");
            int choice = getIntInput("Choice: ");
            switch (choice) {
                case 1 -> {
                    Collection<Transaction> all = lib.getAllTransactions();
                    if (all.isEmpty()) { System.out.println(YELLOW + "No transactions." + RESET); break; }
                    printTransactionHeader();
                    all.forEach(System.out::println);
                    System.out.println(CYAN + "Total: " + all.size() + RESET);
                }
                case 2 -> {
                    List<Transaction> active = lib.getActiveTransactions();
                    if (active.isEmpty()) { System.out.println(YELLOW + "No active borrows." + RESET); break; }
                    printTransactionHeader();
                    active.forEach(System.out::println);
                    System.out.println(CYAN + "Active: " + active.size() + RESET);
                }
                case 3 -> {
                    List<Transaction> overdue = lib.getOverdueTransactions();
                    if (overdue.isEmpty()) { System.out.println(GREEN + "No overdue books! 🎉" + RESET); break; }
                    System.out.println(RED + "⚠  OVERDUE BOOKS:" + RESET);
                    printTransactionHeader();
                    overdue.forEach(t -> System.out.println(RED + t + RESET));
                    System.out.println(RED + "Total overdue: " + overdue.size() + RESET);
                }
                case 4 -> {
                    String mid = getStringInput("Member ID: ");
                    List<Transaction> mt = lib.getMemberTransactions(mid);
                    if (mt.isEmpty()) { System.out.println(YELLOW + "No transactions for this member." + RESET); break; }
                    printTransactionHeader();
                    mt.forEach(System.out::println);
                }
                case 5 -> {
                    String bid = getStringInput("Book ID: ");
                    List<Transaction> bt = lib.getBookTransactions(bid);
                    if (bt.isEmpty()) { System.out.println(YELLOW + "No transactions for this book." + RESET); break; }
                    printTransactionHeader();
                    bt.forEach(System.out::println);
                }
                case 0 -> { return; }
                default -> System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }

    // ─── DASHBOARD ──────────────────────────────────────────────
    static void showDashboard() {
        System.out.println(CYAN + BOLD);
        System.out.println("\n╔═══════════════════════════════════════════════╗");
        System.out.println("║           📊  LIBRARY DASHBOARD               ║");
        System.out.println("╚═══════════════════════════════════════════════╝" + RESET);
        System.out.printf("  %-28s : %s%d%s%n", "📚 Total Books",       YELLOW, lib.totalBooks(),         RESET);
        System.out.printf("  %-28s : %s%d%s%n", "👤 Total Members",     YELLOW, lib.totalMembers(),       RESET);
        System.out.printf("  %-28s : %s%d%s%n", "🔄 Total Transactions",YELLOW, lib.totalTransactions(),  RESET);
        System.out.printf("  %-28s : %s%d%s%n", "📖 Active Borrows",    GREEN,  lib.activeTransactions(), RESET);
        System.out.printf("  %-28s : %s%d%s%n", "⚠  Overdue Books",     RED,    lib.overdueCount(),       RESET);
        System.out.printf("  %-28s : %sRs.%.2f%s%n","💰 Fines Collected", GREEN, lib.totalFinesCollected(),RESET);
        System.out.printf("  %-28s : %sRs.%.2f%s%n","⏳ Pending Fines",   RED,   lib.pendingFines(),       RESET);
        System.out.println(CYAN + "  ─────────────────────────────────────────────" + RESET);
        System.out.println("  Date : " + LocalDate.now());
        System.out.println(CYAN + "═════════════════════════════════════════════════" + RESET);
    }

    // ─── PRINT HELPERS ──────────────────────────────────────────
    static void printBookHeader() {
        System.out.println(BOLD + CYAN);
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-10s %-35s %-22s %-15s %-8s %s%n",
                "ID", "Title", "Author", "Genre", "Avail/Total", "Year");
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────" + RESET);
    }

    static void printMemberHeader() {
        System.out.println(BOLD + CYAN);
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-10s %-22s %-28s %-14s %-10s %-12s %s%n",
                "ID", "Name", "Email", "Phone", "Type", "JoinDate", "Status");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────────" + RESET);
    }

    static void printTransactionHeader() {
        System.out.println(BOLD + CYAN);
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-14s %-10s %-10s %-12s %-12s %-14s %s%n",
                "TxnID", "MemberID", "BookID", "IssueDate", "DueDate", "ReturnDate", "Fine");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────" + RESET);
    }

    // ─── INPUT HELPERS ──────────────────────────────────────────
    static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    static String getStringInputOptional(String prompt, String defaultVal) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultVal : input;
    }

    static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println(RED + "Please enter a valid number." + RESET);
            }
        }
    }

    static int getIntInputOptional(String prompt, int defaultVal) {
        System.out.print(prompt);
        try {
            String line = scanner.nextLine().trim();
            return line.isEmpty() ? defaultVal : Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}