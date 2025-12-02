package main;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

/**
 * Simple DB seeder to ensure a test account exists for development.
 *
 * What this class does:
 * - Creates the "users" table if it does not exist.
 * - Checks if the test user is already in the database.
 * - If not, inserts the test user with a hashed password.
 *
 * Test credentials (only for development):
 *    username: testuser
 *    password: Test1234!
 *
 * NOTE:
 * This class is only for developer testing convenience.
 * Do NOT enable or run this in production.
 */
public final class DatabaseSeeder {

    // SQLite database file used by the app.
    private static final String DB_URL = "jdbc:sqlite:lamesa.db";

    // SQL to create the users table if it doesn’t exist.
    // Matches the structure used by the rest of the project.
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL" +
                    ");";

    // Private constructor prevents creating an instance of this utility class.
    private DatabaseSeeder() {}

    /**
     * Ensures the test user exists in the database.
     * This method:
     * 1. Makes sure the users table exists.
     * 2. Checks if the test user is already present.
     * 3. Inserts the test user if missing.
     */
    public static void seedTestUser() {
        final String testUsername = "1";
        final String testPassword = "1";

        // Connect to the SQLite database (auto-closes because of try-with-resources).
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // 1. Create the users table if needed.
            try (PreparedStatement stmt = conn.prepareStatement(CREATE_USERS_TABLE)) {
                stmt.execute();
            }

            // 2. Check if the test user already exists.
            String checkSql = "SELECT COUNT(1) AS cnt FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, testUsername);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("cnt") > 0) {
                        // User already exists → no need to insert again.
                        System.out.println("[DatabaseSeeder] Test user '" + testUsername + "' already exists.");
                        return;
                    }
                }
            }

            // 3. Insert the test user with a SHA-256 hashed password.
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, testUsername);
                ps.setString(2, hashPassword(testPassword)); // Hashing done below
                ps.executeUpdate();

                System.out.println("[DatabaseSeeder] Inserted test user '" + testUsername + "'.");
            }

        } catch (SQLException e) {
            // Any SQL/database-related error ends up here.
            System.err.println("[DatabaseSeeder] Database error while seeding test user:");
            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {
            // Happens if SHA-256 is somehow missing (unlikely).
            System.err.println("[DatabaseSeeder] Hashing algorithm error while seeding test user:");
            e.printStackTrace();
        }
    }

    /**
     * Hashes a plain-text password using SHA-256.
     * Steps:
     *  - Convert password → bytes
     *  - Hash using MessageDigest
     *  - Convert hash to a 64-character hex string
     *
     * @param password the plain-text password
     * @return hashed password string (hex)
     */
    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Hash the password bytes
        byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));

        // Convert to positive BigInteger so we can turn it into hex text
        BigInteger n = new BigInteger(1, digest);
        String hex = n.toString(16);

        // Ensure 64-character length by padding with zeros if needed
        while (hex.length() < 64) hex = "0" + hex;

        return hex;

        
    }

    // CREATION OF INVENTORY TABLE 
    private static final String CREATE_INVENTORY_TABLE = 
        "CREATE TABLE IF NOT EXISTS inventory ( " +         //   Table name
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "product_name TEXT NOT NULL," +
            "category TEXT NOT NULL, " +
            "type TEXT NOT NULL, "  +
            "instruction TEXT NOT NULL, " +
            "stock_quantity INTEGER DEFAULT 0, " +
            "status TEXT DEFAULT 'No Worries Yet' " +
            ");";
        
    public static void seedInventory() {
        try(Connection conn = DriverManager.getConnection(DB_URL)) {
            
            // 1. Create the inventory table if needed.
            try (PreparedStatement stmt = conn.prepareStatement(CREATE_INVENTORY_TABLE)) {
                stmt.execute();
            } 
            
        String insertSql = "INSERT INTO inventory (product_name, category, type, instruction, stock_quantity, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            // Row 1 sample
            ps.setString(1, "Wagyu Tapa");
            ps.setString(2, "Breakfast");
            ps.setString(3, "Non-Vegetarian");
            ps.setString(4, "Low in Stock");
            ps.setInt(5, 10);
            ps.setString(6, "Pending");
            ps.executeUpdate();

            // Row 2
            ps.setString(1, "Iberico Tocino");
            ps.setString(2, "Breakfast");
            ps.setString(3, "Non-Vegetarian");
            ps.setString(4, "In Stock");
            ps.setInt(5, 25);
            ps.setString(6, "");
            ps.executeUpdate();

            // Row 3
            ps.setString(1, "Ostrich Tortang Talong");
            ps.setString(2, "Breakfast");
            ps.setString(3, "Non-Vegetarian");
            ps.setString(4, "High in Stock");
            ps.setInt(5, 50);
            ps.setString(6, "");
            ps.executeUpdate();

            System.out.println("[DatabaseSeeder] Inventory seeded successfully. ");
        }
        
            } catch (SQLException e) {
                e.printStackTrace();
        } 
    }
}