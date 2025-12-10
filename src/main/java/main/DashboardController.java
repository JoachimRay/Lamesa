package main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {
    
    @FXML
    private Label welcomeLabel;

    public void setUsername(String username) {
        welcomeLabel.setText("Hi, " + username + ". Welcome Back to Lamesa Admin");
        
    }
}
