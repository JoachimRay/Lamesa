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
 * LoginController
 *
 * Responsibilities:
 * 1. Handle login form input validation
 * 2. Authenticate user credentials
 * 3. Store session info (username + role) for RBAC
 * 4. Record attendance login timestamp
 * 5. Navigate to main UI
 *
 * FIXED ISSUES:
 * - Changed database path from "Database/lamesa.db" to "database/lamesa.db" (lowercase)
 * - Fixed database locking issue by closing connection before calling AttendanceUtils
 */
public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordFieldVisible;

    private boolean isPasswordVisible = false;

    private static final String DB_URL = "jdbc:sqlite:database/lamesa.db";

    public LoginController() {
        checkDatabase();
    }

    /**
     * Check if the SQLite database file exists.
     */
    private void checkDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("[LoginController] Database exists.");
        } catch (SQLException e) {
            System.err.println("[LoginController] Database check failed: " + e.getMessage());
        }
    }

    /**
     * Switch to the registration screen.
     */
    @FXML
    private void switchToRegister() {
        System.out.println("[LoginController] switchToRegister()");
        App.setRoot("register");
    }

    /**
     * Toggle password field visibility between hidden and visible.
     */
    @FXML
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordFieldVisible.setVisible(false);
            passwordField.setVisible(true);
            passwordField.setText(passwordFieldVisible.getText());
            isPasswordVisible = false;
        } else {
            passwordFieldVisible.setVisible(true);
            passwordField.setVisible(false);
            passwordFieldVisible.setText(passwordField.getText());
            isPasswordVisible = true;
        }
    }

    /**
     * Handles login button click.
     * Steps:
     * 1. Validate username and password fields.
     * 2. Check database for matching credentials.
     * 3. If login successful:
     * - Store username and role in session (RBAC).
     * - Record attendance login timestamp via AttendanceUtils.
     * - Navigate to the main UI.
     * 4. Show error alerts for invalid credentials or DB errors.
     */
    @FXML
    private void handleLogin() {
        System.out.println("[LoginController] ===== LOGIN ATTEMPT START =====");
        
        // 1. Get user input
        String username = (usernameField == null) ? "" : usernameField.getText().trim();
        String password = getPasswordInput();

        System.out.println("[LoginController] handleLogin for user='" + username + "'");

        // 2. Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("[LoginController] Validation failed: empty fields");
            showAlert(Alert.AlertType.ERROR, "Validation error", "Username and password must not be empty.");
            return;
        }

        // Variables to store authentication results
        String authenticatedRole = null;
        boolean authSuccess = false;

        // 3. Connect to DB and check credentials
        // CRITICAL FIX: Use try-with-resources to ensure connection is closed before calling AttendanceUtils
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("[LoginController] Database connection successful");

            String sql = "SELECT password_hash, role FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[LoginController] User found in database");
                        
                        String storedHash = rs.getString("password_hash");
                        String providedHash = hashPassword(password);

                        // 4. Validate password
                        if (!storedHash.equals(providedHash)) {
                            System.out.println("[LoginController] Password mismatch!");
                            showAlert(Alert.AlertType.ERROR, "Login failed", "Invalid username or password.");
                            return;
                        }

                        // 5. Authentication successful
                        System.out.println("[LoginController] ===== AUTHENTICATION SUCCESSFUL =====");
                        authenticatedRole = rs.getString("role");
                        authSuccess = true;
                        System.out.println("[LoginController] User role: " + authenticatedRole);

                    } else {
                        // User not found
                        System.out.println("[LoginController] User not found in database");
                        showAlert(Alert.AlertType.ERROR, "Login failed", "Invalid username or password.");
                        return;
                    }
                }
            }
            
            // Connection will be automatically closed here when exiting try-with-resources
            
        } catch (SQLException e) {
            System.err.println("[LoginController] SQL Exception during login:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database error", e.getMessage());
            return;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("[LoginController] Encryption error:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Encryption error", e.getMessage());
            return;
        }

        // CRITICAL FIX: Database connection is now closed, safe to call AttendanceUtils
        if (authSuccess && authenticatedRole != null) {
            // 6. Store session info (username + role) for RBAC
            System.out.println("[LoginController] Setting session...");
            SessionManager.setCurrentUser(username, authenticatedRole);
            System.out.println("[LoginController] Session set. Current user: " + SessionManager.getUsername());

            // 7. Record login timestamp using AttendanceUtils
            // Connection is closed, so no database lock conflict
            System.out.println("[LoginController] About to call AttendanceUtils.recordLogin()...");
            try {
                AttendanceUtils.recordLogin(username);
                System.out.println("[LoginController] AttendanceUtils.recordLogin() completed");
            } catch (Exception e) {
                System.err.println("[LoginController] ERROR calling AttendanceUtils.recordLogin():");
                e.printStackTrace();
            }

            // 8. Navigate to main UI
            System.out.println("[LoginController] Navigating to main screen...");
            App.setRoot("main");
            System.out.println("[LoginController] ===== LOGIN PROCESS COMPLETE =====");
        }
    }

    /**
     * Retrieve password input from visible or hidden field.
     */
    private String getPasswordInput() {
        if (passwordFieldVisible != null && passwordFieldVisible.isVisible())
            return passwordFieldVisible.getText();
        if (passwordField != null)
            return passwordField.getText();
        return "";
    }

    /**
     * Hash password using SHA-256.
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
        BigInteger n = new BigInteger(1, digest);
        String hex = n.toString(16);
        while (hex.length() < 64)
            hex = "0" + hex;
        return hex;
    }

    /**
     * Show alert safely on JavaFX thread.
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