package main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SettingsController {

          @FXML private Button logoutBtn;
        
    /**
     * Logs the user out:
     * - Clears all session data (username & role)
     * - Redirects user back to login screen
     */
    @FXML
    private void onLogout() {
        SessionManager.clear();   // Wipes login session
        App.setRoot("login");     // Go back to login page
    }

}
