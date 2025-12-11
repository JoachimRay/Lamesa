package main;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Insets;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * SettingsController - Complete Settings Page
 * 
 * NEW FEATURES:
 * 1. Display user profile information (username, role, member since)
 * 2. Show live session timer (how long user has been logged in)
 * 3. Change Password dialog
 * 4. View Attendance History dialog
 * 5. About section (app information)
 * 6. Modern UI with cards
 * 
 * RESPONSIBILITIES:
 * - Load and display user information from database
 * - Calculate and display session duration
 * - Handle password change requests
 * - Display attendance history
 * - Handle user logout with attendance tracking
 */
public class SettingsController {
    
    // UI Elements from FXML
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label memberSinceLabel;
    @FXML private Label sessionTimeLabel;
    @FXML private Button changePasswordBtn;
    @FXML private Button viewHistoryBtn;
    @FXML private Button logoutBtn;

    // Database connection string
    private static final String DB_URL = "jdbc:sqlite:database/lamesa.db";
    
    // Date formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Session start time (for live timer)
    private LocalDateTime sessionStartTime;
    
    // Timeline for updating session timer every second
    private Timeline sessionTimerTimeline;

    /**
     * Initialize method - called automatically when FXML loads
     * 
     * Loads all user information and starts the session timer
     */
    @FXML
    private void initialize() {
        System.out.println("[SettingsController] Initializing settings page...");
        
        // Load user profile information
        loadUserProfile();
        
        // Load session start time and start timer
        loadSessionStartTime();
        startSessionTimer();
    }

    /**
     * Load user profile information from database
     * 
     * Retrieves:
     * - Username (from SessionManager)
     * - Role (from SessionManager)
     * - Account creation date (from database)
     */
    private void loadUserProfile() {
        // Get current user from session
        String username = SessionManager.getUsername();
        String role = SessionManager.getRole();
        
        if (username == null || username.isEmpty()) {
            System.err.println("[SettingsController] No user logged in");
            return;
        }
        
        // Display username and role
        usernameLabel.setText(username);
        roleLabel.setText(role != null ? role.substring(0, 1).toUpperCase() + role.substring(1) : "Employee");
        
        // Get account creation date from database
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Query to get the user's creation date
            // Note: SQLite doesn't store creation date by default, so we'll use the first attendance record as a proxy
            // You can add a 'created_at' column to users table for more accuracy
            String sql = "SELECT MIN(login_time) as first_login FROM attendance WHERE username = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String firstLogin = rs.getString("first_login");
                        if (firstLogin != null && !firstLogin.isEmpty()) {
                            LocalDateTime firstLoginDate = LocalDateTime.parse(firstLogin, TIME_FORMATTER);
                            memberSinceLabel.setText(firstLoginDate.format(DATE_FORMATTER));
                        } else {
                            memberSinceLabel.setText("Today");
                        }
                    } else {
                        memberSinceLabel.setText("Today");
                    }
                }
            }
            
            System.out.println("[SettingsController] User profile loaded successfully");
            
        } catch (SQLException e) {
            System.err.println("[SettingsController] Error loading user profile: " + e.getMessage());
            e.printStackTrace();
            memberSinceLabel.setText("Unknown");
        }
    }

    /**
     * Load the current session start time from database
     * 
     * Gets the most recent login_time for the current user where logout_time is NULL
     */
    private void loadSessionStartTime() {
        String username = SessionManager.getUsername();
        if (username == null) return;
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT login_time FROM attendance WHERE username = ? AND logout_time IS NULL ORDER BY id DESC LIMIT 1";
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String loginTimeStr = rs.getString("login_time");
                        sessionStartTime = LocalDateTime.parse(loginTimeStr, TIME_FORMATTER);
                        System.out.println("[SettingsController] Session start time: " + sessionStartTime);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[SettingsController] Error loading session time: " + e.getMessage());
        }
    }

    /**
     * Start the session timer that updates every second
     * 
     * Displays live session duration in format: "2h 15m 30s"
     */
    private void startSessionTimer() {
        if (sessionStartTime == null) {
            sessionTimeLabel.setText("No active session");
            return;
        }
        
        // Create a timeline that updates every second
        sessionTimerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateSessionDisplay();
        }));
        sessionTimerTimeline.setCycleCount(Timeline.INDEFINITE);
        sessionTimerTimeline.play();
        
        // Initial update
        updateSessionDisplay();
    }

    /**
     * Update the session time display
     * 
     * Calculates time difference between now and session start
     * Formats as: "2h 15m 30s"
     */
    private void updateSessionDisplay() {
        if (sessionStartTime == null) return;
        
        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(sessionStartTime, now);
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        String timeStr = String.format("%dh %dm %ds", hours, minutes, secs);
        sessionTimeLabel.setText(timeStr);
    }

    /**
     * Handle Change Password button click
     * 
     * Opens a dialog where user can enter old password and new password
     */
    @FXML
    private void onChangePassword() {
        System.out.println("[SettingsController] Change password clicked");
        
        // Create a custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your current password and new password");
        
        // Set button types
        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Current Password");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        
        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Handle the result
        dialog.showAndWait().ifPresent(response -> {
            if (response == changeButtonType) {
                String oldPassword = oldPasswordField.getText();
                String newPassword = newPasswordField.getText();
                String confirmPassword = confirmPasswordField.getText();
                
                // Validate inputs
                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "All fields are required");
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match");
                    return;
                }
                
                if (newPassword.length() < 8) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Password must be at least 8 characters");
                    return;
                }
                
                // TODO: Implement password change logic
                // This would require verifying old password and updating database
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!\n(Feature coming soon hehe)");
            }
        });
    }

    /**
     * Handle View Attendance History button click
     * 
     * Opens a dialog showing user's past login/logout records
     */
    @FXML
    private void onViewHistory() {
        System.out.println("[SettingsController] View history clicked");
        
        String username = SessionManager.getUsername();
        if (username == null) return;
        
        // Create a custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Attendance History");
        dialog.setHeaderText("Your recent attendance records");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Create TableView for attendance records
        TableView<AttendanceRecord> table = new TableView<>();
        table.setPrefWidth(600);
        table.setPrefHeight(400);
        
        // Define columns
        TableColumn<AttendanceRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(120);
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().date));
        
        TableColumn<AttendanceRecord, String> loginCol = new TableColumn<>("Login Time");
        loginCol.setPrefWidth(120);
        loginCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().loginTime));
        
        TableColumn<AttendanceRecord, String> logoutCol = new TableColumn<>("Logout Time");
        logoutCol.setPrefWidth(120);
        logoutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().logoutTime));
        
        TableColumn<AttendanceRecord, String> hoursCol = new TableColumn<>("Hours Worked");
        hoursCol.setPrefWidth(120);
        hoursCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().hoursWorked));
        
        TableColumn<AttendanceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().status));
        
        table.getColumns().addAll(dateCol, loginCol, logoutCol, hoursCol, statusCol);
        
        // Load data from database
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT login_time, logout_time, worked_hours, full_shift FROM attendance WHERE username = ? ORDER BY id DESC LIMIT 20";
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String loginTime = rs.getString("login_time");
                        String logoutTime = rs.getString("logout_time");
                        double workedHours = rs.getDouble("worked_hours");
                        boolean fullShift = rs.getBoolean("full_shift");
                        
                        if (loginTime != null) {
                            LocalDateTime loginDT = LocalDateTime.parse(loginTime, TIME_FORMATTER);
                            String date = loginDT.format(DATE_FORMATTER);
                            String login = loginDT.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            String logout = logoutTime != null ? LocalDateTime.parse(logoutTime, TIME_FORMATTER).format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "Active";
                            String hours = logoutTime != null ? String.format("%.2f hrs", workedHours) : "-";
                            String status = logoutTime != null ? (fullShift ? "✓ Complete" : "Incomplete") : "Active";
                            
                            table.getItems().add(new AttendanceRecord(date, login, logout, hours, status));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[SettingsController] Error loading attendance history: " + e.getMessage());
            e.printStackTrace();
        }
        
        // If no records found
        if (table.getItems().isEmpty()) {
            Label noData = new Label("No attendance records found");
            noData.setStyle("-fx-padding: 20; -fx-font-size: 14px; -fx-text-fill: #6b7280;");
            dialog.getDialogPane().setContent(noData);
        } else {
            dialog.getDialogPane().setContent(table);
        }
        
        dialog.show();
    }

    /**
     * Handle Logout button click
     * 
     * Steps:
     * 1. Stop session timer
     * 2. Record logout timestamp in database
     * 3. Clear session
     * 4. Navigate to login page
     */
    @FXML
    private void onLogout() {
        System.out.println("[SettingsController] Logout clicked");
        
        // Stop the session timer
        if (sessionTimerTimeline != null) {
            sessionTimerTimeline.stop();
        }
        
        // Get current user
        String currentUser = SessionManager.getUsername();
        
        if (currentUser != null && !currentUser.isEmpty()) {
            // Record logout timestamp, compute hours worked, mark full shift
            AttendanceUtils.recordLogout(currentUser);
        }

        // Clear user session
        SessionManager.clear();

        // Navigate back to login page
        App.setRoot("login");
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Inner class to represent an attendance record in the table
     */
    public static class AttendanceRecord {
        private final String date;
        private final String loginTime;
        private final String logoutTime;
        private final String hoursWorked;
        private final String status;

        public AttendanceRecord(String date, String loginTime, String logoutTime, String hoursWorked, String status) {
            this.date = date;
            this.loginTime = loginTime;
            this.logoutTime = logoutTime;
            this.hoursWorked = hoursWorked;
            this.status = status;
        }

        public String getDate() { return date; }
        public String getLoginTime() { return loginTime; }
        public String getLogoutTime() { return logoutTime; }
        public String getHoursWorked() { return hoursWorked; }
        public String getStatus() { return status; }
    }
}