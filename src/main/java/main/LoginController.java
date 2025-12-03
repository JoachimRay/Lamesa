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
 * Notes:
 * - Attendance is tracked in a separate 'attendance' table.
 * - Each login creates a new attendance row with current timestamp.
 * - Passwords are hashed using SHA-256 (replace with bcrypt/argon2 for
 * production).
 */
public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField; // hidden password input
    @FXML
    private TextField passwordFieldVisible; // visible password input

    private boolean isPasswordVisible = false; // toggle state

    private static final String DB_URL = "jdbc:sqlite:Database/lamesa.db";

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
        // 1. Get user input
        String username = (usernameField == null) ? "" : usernameField.getText().trim();
        String password = getPasswordInput(); // Either visible or hidden field

        System.out.println("[LoginController] handleLogin for user='" + username + "'");

        // 2. Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation error", "Username and password must not be empty.");
            return;
        }

        // 3. Connect to DB and check credentials
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "SELECT password_hash, role FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        String providedHash = hashPassword(password);

                        // 4. Validate password
                        if (!storedHash.equals(providedHash)) {
                            showAlert(Alert.AlertType.ERROR, "Login failed", "Invalid username or password.");
                            return;
                        }

                        // 5. Authentication successful
                        String role = rs.getString("role");
                        System.out.println("[LoginController] User role: " + role);

                        // 6. Store session info (username + role) for RBAC
                        SessionManager.setCurrentUser(username, role);

                        // 7. Record login timestamp using AttendanceUtils
                        AttendanceUtils.recordLogin(username);

                        // 8. Navigate to main UI
                        App.setRoot("main");
                        return;

                    } else {
                        // User not found
                        showAlert(Alert.AlertType.ERROR, "Login failed", "Invalid username or password.");
                        return;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database error", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Encryption error", e.getMessage());
        }
    }

    /**
     * Records a login timestamp for attendance tracking using AttendanceUtils.
     */
    private void recordAttendance(Connection conn, String username) {
        AttendanceUtils.recordLogin(username);
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
