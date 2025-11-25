package main;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField; 

// JDBC imports for database connectivity
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;


public class RegisterController {

    /** Username input field from register.fxml */
    @FXML 
    private TextField usernameField; 
    
    /** Password input field (shows as bullet points) */
    @FXML 
    private PasswordField passwordField; 

    /** Password input field alternative (shows plain text when toggled) */
    @FXML 
    private TextField passwordFieldVisible;

    /** Confirm password input field (shows as bullet points) */
    @FXML 
    private PasswordField confirmPasswordField; 

    /** Confirm password input field alternative (shows plain text when toggled) */
    @FXML
    private TextField confirmPasswordFieldVisible; 

    // ============ STATE VARIABLES ============
    /** Tracks whether main password is currently visible as plain text */
    private boolean isPasswordVisible = false; 

    /** Tracks whether confirm password is currently visible as plain text */
    private boolean isConfirmPasswordVisible = false; 

    /** Stores the new username entered by user */
    private String Newuser = ""; 
    
    /** Stores the new password entered by user */
    private String Newpass = "";
    
    /** Stores the confirm password value to verify they match */
    private String confirmpass = "";

    // ============ DATABASE CONFIGURATION ============
    /** SQLite database connection URL */
    String url = "database link here";

    /** SQL statement for inserting new account into accounts table */
    String add_account = "SQLite statements for adding to accounts table here";  

    // ============ NAVIGATION METHODS ============
    /**
     * Switches the scene from register page back to login page
     * Called when user clicks "Login" button
     */
    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }

 
    /**
     * Toggles main password field between visible (plain text) and hidden (bullet points)
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
        } else {
            // Show plain text version, hide password bullets
            passwordFieldVisible.setVisible(true); 
            passwordField.setVisible(false); 
            passwordFieldVisible.setText(passwordField.getText());
            isPasswordVisible = true; 
        }
    }

    /**
     * Toggles confirm password field between visible (plain text) and hidden (bullet points)
     * Uses StackPane overlay to keep both fields in exact same position
     * Synchronizes text when switching between PasswordField and TextField
     */
    @FXML
    private void toggleConfirmPasswordVisibility(){ 
        if(isConfirmPasswordVisible){
            // Hide plain text version, show password bullets
            confirmPasswordFieldVisible.setVisible(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setText(confirmPasswordFieldVisible.getText());
            isConfirmPasswordVisible = false; 
        } else {
            // Show plain text version, hide password bullets
            confirmPasswordFieldVisible.setVisible(true);
            confirmPasswordField.setVisible(false); 
            confirmPasswordFieldVisible.setText(confirmPasswordField.getText());
            isConfirmPasswordVisible = true;
        }
    }

  
    // TODO: Store password first to compare before storing to database
    // TODO: Add password validation function here
    // TODO: Add account registration button handler here
    



    // Button for register to add account to database if everything is correct







}