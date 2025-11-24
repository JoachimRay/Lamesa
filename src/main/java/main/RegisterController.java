package main;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField; 

public class RegisterController {

    @FXML 
    private TextField usernameField; 
    
    @FXML 
    private PasswordField passwordField; 

    @FXML 
    private TextField passwordFieldVisible;

    @FXML 
    private PasswordField confirmPasswordField; 

    @FXML
    private TextField confirmPasswordFieldVisible; 


    private boolean isPasswordVisible = false; 

    private String Newuser = ""; 
    private String Newpass = "";
    private String confirmpass = "";



    @FXML // Switch to login page
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }



    @FXML 
    private void togglePasswordVisibility(){ 
        if(isPasswordVisible){ 
            passwordFieldVisible.setVisible(false);
            passwordField.setVisible(true);
            passwordField.setText(passwordFieldVisible.getText());
            isPasswordVisible = false;

        } else 
        {
            passwordFieldVisible.setVisible(true); 
            passwordField.setVisible(false); 
            passwordFieldVisible.setText(passwordField.getText());
            isPasswordVisible = true; 
        }
    }

    private boolean isConfirmPasswordVisible = false; 
    

    @FXML
    private void toggleConfirmPasswordVisibility(){ 
        if(isConfirmPasswordVisible){
            confirmPasswordFieldVisible.setVisible(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setText(confirmPasswordFieldVisible.getText());
            isConfirmPasswordVisible = false; 
        } else 
        {
            confirmPasswordFieldVisible.setVisible(true);
            confirmPasswordField.setVisible(false); 
            confirmPasswordFieldVisible.setText(confirmPasswordField.getText());
            isConfirmPasswordVisible = true;
        }
    }







}