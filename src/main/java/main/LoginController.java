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



public class LoginController {


    @FXML 
    private TextField usernameField; 
    @FXML
    private PasswordField passwordField; 
    @FXML
    private TextField passwordFieldVisible;

    private boolean isPasswordVisible = false; 

    
    // Link to database in the database folder
    String url = "database link here";

    // SQL statement for checking if the account exists by comparing it to the database
    String check_account = "SQLite statement to check whether account exists here";  



    @FXML
    private void switchToRegister() throws IOException {
        App.setRoot("register");
    }

    @FXML
    private void togglePasswordVisibility(){
        if(isPasswordVisible){
            passwordFieldVisible.setVisible(false); 
            passwordField.setVisible(true); 
            passwordField.setText(passwordFieldVisible.getText()); 
            isPasswordVisible = false;
        }   
        else {
            passwordFieldVisible.setVisible(true);
            passwordField.setVisible(false);
            passwordFieldVisible.setText(passwordField.getText());
            isPasswordVisible = true;
        }
    }


    // Store password and username to compare with database before allowing to login 











}
