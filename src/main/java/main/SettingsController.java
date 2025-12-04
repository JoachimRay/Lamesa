package main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * SettingsController
 * 
 * Responsibilities:
 * - Handles user logout
 * - Tracks attendance by recording logout time
 * - Calculates hours worked and determines if full shift completed
 * - Clears session data and navigates back to login screen
 */
public class SettingsController {
    
    @FXML private Button logoutBtn;

    /**
     * Triggered when the Logout button is clicked.
     * Steps:
     * 1. Get the current logged-in user from SessionManager.
     * 2. Record logout timestamp, calculate worked hours, and mark full shift using AttendanceUtils.
     * 3. Clear session info (username and role).
     * 4. Navigate back to the login screen.
     */
    @FXML
    private void onLogout() {
        String currentUser = SessionManager.getUsername(); // Current logged-in user
        if (currentUser != null && !currentUser.isEmpty()) {
            // Record logout timestamp, compute hours worked, mark full shift
            AttendanceUtils.recordLogout(currentUser);
        }

        // Clear user session
        SessionManager.clear();

        // Navigate back to login page
        App.setRoot("login");
    }
}
