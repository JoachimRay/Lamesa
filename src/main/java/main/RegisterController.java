package main;

import javafx.application.Platform;
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
 * Register controller with proper form validation.
 *
 * Notes:
 * - Removed the development "dev/dev" fallback account creation.
 * - Adds strict validation rules:
 *     - username: required, minimum length 3, no whitespace
 *     - password: required, minimum length 8, must include upper/lower/digit/special
 *     - confirm: must match password
 * - On successful registration the app navigates to the main shell (main.fxml).
 */
public class RegisterController {

    // FXML-bound input fields (password fields have both hidden + visible variants)
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;               // hidden password entry
    @FXML private TextField passwordFieldVisible;            // visible password entry (when "show" toggled)
    @FXML private PasswordField confirmPasswordField;        // hidden confirm password entry
    @FXML private TextField confirmPasswordFieldVisible;     // visible confirm entry (when "show" toggled)

    // Track visibility state for both password and confirm fields
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Database configuration and table DDL
    private static final String DB_URL = "jdbc:sqlite:database/lamesa.db";
  
    /**
     * Constructor - checks if database exists on controller load.
     */
    public RegisterController() {
        checkDatabase();
    }

    /**
     * Checks if the database file exists.
     */
    private void checkDatabase() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            conn.close();
            System.out.println("[RegisterController] Database exists.");
        } catch (SQLException e) {
            System.err.println("[RegisterController] Database check failed: " + e.getMessage());
        }
    }

    /**
     * Switch back to the login screen.
     * Called from the "Already have an account? Login" action.
     */
    @FXML
    private void switchToLogin() {
        System.out.println("[RegisterController] switchToLogin()");
        try {
            App.setRoot("login");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation error", "Unable to switch to login: " + e.getMessage());
        }
    }

    /**
     * Toggle password visibility for the main password field.
     * Copies text between the hidden and visible fields so the value is preserved.
     */
    @FXML
    private void togglePasswordVisibility(){
        if(isPasswordVisible){
            // Switch to hidden (PasswordField)
            passwordFieldVisible.setVisible(false);
            passwordField.setVisible(true);
            passwordField.setText(passwordFieldVisible.getText());
            isPasswordVisible = false;
        } else {
            // Switch to visible (TextField)
            passwordFieldVisible.setVisible(true);
            passwordField.setVisible(false);
            passwordFieldVisible.setText(passwordField.getText());
            isPasswordVisible = true;
        }
    }

    /**
     * Toggle password visibility for the confirm password field.
     * Works the same way as togglePasswordVisibility().
     */
    @FXML
    private void toggleConfirmPasswordVisibility(){
        if(isConfirmPasswordVisible){
            confirmPasswordFieldVisible.setVisible(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setText(confirmPasswordFieldVisible.getText());
            isConfirmPasswordVisible = false;
        } else {
            confirmPasswordFieldVisible.setVisible(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordFieldVisible.setText(confirmPasswordField.getText());
            isConfirmPasswordVisible = true;
        }
    }

    /**
     * Main registration flow:
     * 1. Read inputs
     * 2. Validate inputs (validateInputs())
     * 3. Check for existing username
     * 4. Insert new user with hashed password
     * 5. Navigate to the main app shell
     */
    @FXML
    private void NextRegister() {
        String username = (usernameField == null) ? "" : usernameField.getText().trim();
        String password = getPasswordInput();
        String confirm = getConfirmPasswordInput();

        System.out.println("[RegisterController] registerUser for '" + username + "'");

        // 1. Validate inputs; validateInputs returns null when valid, or an error message.
        String validationError = validateInputs(username, password, confirm);
        if (validationError != null) {
            showAlert(Alert.AlertType.ERROR, "Validation error", validationError);
            return;
        }

        // 2. Database operations (check then insert)
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Check if username already exists
            String checkSql = "SELECT COUNT(1) AS cnt FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("cnt") > 0) {
                        showAlert(Alert.AlertType.ERROR, "Registration failed", "Username already exists.");
                        return;
                    }
                }
            }

            // Insert the new user with a hashed password
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, username);
                ps.setString(2, hashPassword(password)); // Hash with SHA-256 (not ideal for prod)
                ps.executeUpdate();
            }

            // Inform user and navigate into the app
            showAlert(Alert.AlertType.INFORMATION, "Registration successful", "Account created. Signing you in...");

            // Platform.runLater to ensure navigation occurs on the JavaFX Application Thread.
            Platform.runLater(() -> {
                try {
                    App.setRoot("choice");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Navigation error", "Unable to open main application: " + e.getMessage());
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database error", "An error occurred while accessing the database.");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Encryption error", "Unable to process the password hash.");
        }

    }

    /**
     * Validates registration inputs and returns null when valid, or an error message when invalid.
     * Checks:
     *  - username: required, min length 3, no whitespace
     *  - password: required, min length 8, contains lower/upper/digit/special
     *  - confirm: required and matches password
     */
    private String validateInputs(String username, String password, String confirm) {
        if (username == null || username.isEmpty()) {
            return "Username is required.";
        }
        if (username.length() < 3) {
            return "Username must be at least 3 characters long.";
        }
        if (username.matches(".*\\s+.*")) {
            return "Username must not contain whitespace.";
        }

        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }
        StringBuilder pwErr = new StringBuilder();
        if (!password.matches(".*[a-z].*")) pwErr.append("one lowercase letter, ");
        if (!password.matches(".*[A-Z].*")) pwErr.append("one uppercase letter, ");
        if (!password.matches(".*\\d.*")) pwErr.append("one digit, ");
        if (!password.matches(".*[^A-Za-z0-9].*")) pwErr.append("one special character, ");
        if (pwErr.length() > 0) {
            // remove trailing comma+space and return a readable message
            String details = pwErr.toString().replaceAll(", $", "");
            return "Password must contain at least " + details + ".";
        }

        if (confirm == null || confirm.isEmpty()) {
            return "Please confirm your password.";
        }
        if (!password.equals(confirm)) {
            return "Passwords do not match.";
        }

        return null; // inputs are valid
    }

    /**
     * Helper to get the password text depending on which field is visible.
     */
    private String getPasswordInput() {
        if (passwordFieldVisible != null && passwordFieldVisible.isVisible()) {
            return passwordFieldVisible.getText();
        } else if (passwordField != null) {
            return passwordField.getText();
        } else {
            return "";
        }
    }

    /**
     * Helper to get the confirm password text depending on which field is visible.
     */
    private String getConfirmPasswordInput() {
        if (confirmPasswordFieldVisible != null && confirmPasswordFieldVisible.isVisible()) {
            return confirmPasswordFieldVisible.getText();
        } else if (confirmPasswordField != null) {
            return confirmPasswordField.getText();
        } else {
            return "";
        }
    }

    /**
     * Hashes a password using SHA-256 and returns a 64-character hex string.
     * NOTE: For production use a slow password hashing algorithm (bcrypt/argon2).
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        // SHA-256 hashing (still not ideal for production; prefer bcrypt/argon2)
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
        BigInteger n = new BigInteger(1, digest);
        String hex = n.toString(16);
        while (hex.length() < 64) hex = "0" + hex;
        return hex;
    }

    /**
     * Shows an alert on the JavaFX Application Thread.
     * Uses Platform.runLater to ensure thread safety.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }
}
