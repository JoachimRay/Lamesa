package main;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * Login controller.
 * - Handles login form actions (login button, register button, show/hide password).
 * - Validates input, checks credentials in DB, and navigates to the main UI on success.
 */
public class LoginController {

    // FXML-linked UI components
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;       // Hidden password input
    @FXML private TextField passwordFieldVisible;    // Visible password input (when "show" is enabled)

    // Used to track if password should be visible to the user
    private boolean isPasswordVisible = false;

    // SQLite database file location
    private static final String DB_URL = "jdbc:sqlite:lamesa.db";

    // Create users table if it does not exist yet
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL" +
                    ");";

    /**
     * Constructor.
     * Runs before the UI is fully loaded.
     * Ensures the database and users table are available.
     */
    public LoginController() {
        try {
            ensureDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the users table if needed.
     * Called only once during controller construction.
     */
    private void ensureDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(CREATE_USERS_TABLE)) {

            stmt.execute(); // Ensure table is ready
        }
    }

    /**
     * Switches the UI to the registration screen.
     * Triggered when user clicks the "Register" button.
     */
    @FXML
    private void switchToRegister() {
        System.out.println("[LoginController] switchToRegister()");
        App.setRoot("register"); // Navigate to register.fxml
    }

    /**
     * Toggles between hidden and visible password fields.
     * This allows the user to see what they typed.
     */
    @FXML
    private void togglePasswordVisibility() {

        // If currently visible, switch back to hidden
        if (isPasswordVisible) {
            passwordFieldVisible.setVisible(false);
            passwordField.setVisible(true);

            // Copy the text back to hidden field
            passwordField.setText(passwordFieldVisible.getText());
            isPasswordVisible = false;

        } else { // If currently hidden, switch to visible
            passwordFieldVisible.setVisible(true);
            passwordField.setVisible(false);

            // Copy the text to the visible field
            passwordFieldVisible.setText(passwordField.getText());
            isPasswordVisible = true;
        }
    }

    /**
     * Handles the login button.
     * Steps:
     * 1. Read username and password from UI
     * 2. Validate user input
     * 3. Check database for matching credentials
     * 4. If valid, navigate to main UI
     * 5. Otherwise, show an error message
     */
    @FXML
    private void handleLogin() {
        // Safely get username
        String username = (usernameField == null) ? "" : usernameField.getText().trim();
        String password = getPasswordInput(); // Handles visible or hidden field

        System.out.println("[LoginController] handleLogin for user='" + username + "'");

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation error", "Username and password must not be empty.");
            return;
        }

        // Database lookup
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "SELECT password_hash FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, username);

                // Query the DB for stored password hash
                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        // User exists → check password
                        String storedHash = rs.getString("password_hash");
                        String providedHash = hashPassword(password);

                        if (storedHash.equals(providedHash)) {
                            // Login success
                            System.out.println("[LoginController] Authentication success for '" + username + "'");

                            // Attempt to navigate to main UI
                            boolean navigated = App.setRoot("main");
                            if (!navigated) {
                                showAlert(Alert.AlertType.ERROR, "Navigation failed", "Could not open main UI. See console.");
                            }
                            return;

                        } else {
                            // Wrong password
                            showAlert(Alert.AlertType.ERROR, "Login failed", "Invalid username or password.");
                            return;
                        }
                    } else {
                        // No such user
                        showAlert(Alert.AlertType.ERROR, "Login failed", "Invalid username or password.");
                        return;
                    }
                }
            }

        } catch (SQLException e) {
            // Database connection or query failed
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database error", e.getMessage());

        } catch (NoSuchAlgorithmException e) {
            // Hashing algorithm failed (very rare)
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Encryption error", e.getMessage());
        }
    }

    /**
     * Gets the password depending on which input field is currently active.
     */
    private String getPasswordInput() {
        if (passwordFieldVisible != null && passwordFieldVisible.isVisible())
            return passwordFieldVisible.getText();

        if (passwordField != null)
            return passwordField.getText();

        return "";
    }

    /**
     * Hashes a password using SHA-256.
     * Returns a 64-character hex string.
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
        BigInteger n = new BigInteger(1, digest);
        String hex = n.toString(16);

        // Ensure hash is always 64 characters
        while (hex.length() < 64) hex = "0" + hex;

        return hex;
    }

    /**
     * Small helper method to show alerts safely on the JavaFX thread.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }
}
