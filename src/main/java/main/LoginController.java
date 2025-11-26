package main;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


// JDBC imports 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * Alternate/experimental login controller used during development
 * Renamed to avoid conflicting with the main `LoginController` class.
*/
public class LoginController {

    // ============ FXML FIELD BINDINGS ============
    /** Username input field from login.fxml */
    @FXML 
    private TextField usernameField; 
    
    /** Password input field (shows as bullet points) */
    @FXML
    private PasswordField passwordField; 
    
    /** Password input field alternative (shows plain text when toggled) */
    @FXML
    private TextField passwordFieldVisible;

    // ============ STATE VARIABLES ============
    /** Tracks whether password is currently visible as plain text */
    private boolean isPasswordVisible = false; 

    // ============ DATABASE CONFIGURATION ============
    // TODO: Add database URL and SQL statements here when wiring JDBC

    // ============ NAVIGATION METHODS ============
    /**
     * Switches the scene from login page to register page
     * Called when user clicks "Register" button
     */
    @FXML
    private void switchToRegister() throws IOException {
        App.setRoot("register");
    }

    // ============ PASSWORD VISIBILITY TOGGLE METHOD ============
    /**
     * Toggles password field between visible (plain text) and hidden (bullet points)
     * Uses StackPane overlay to keep both fields in exact same position
     * Synchronizes text when switching between PasswordField and TextField
     */
    @FXML
    private void togglePasswordVisibility(){
        if(isPasswordVisible){
            // Hide plain text version, show password bullets
            passwordFieldVisible.setVisible(false); 
            passwordField.setVisible(true); 
            passwordField.setText(passwordFieldVisible.getText()); 
            isPasswordVisible = false;
        }   
        else {
            // Show plain text version, hide password bullets
            passwordFieldVisible.setVisible(true);
            passwordField.setVisible(false);
            passwordFieldVisible.setText(passwordField.getText());
            isPasswordVisible = true;
        }
    }

    // ============ TODO: DATABASE METHODS ============
    // TODO: Store username and password to validate against database
    // TODO: Add login authentication method here
    // TODO: Add error handling for invalid credentials
}
