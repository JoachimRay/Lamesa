package main;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField; 


public class LoginController {


    @FXML 
    private TextField usernameField; 
    @FXML
    private PasswordField passwordField; 
    @FXML
    private TextField passwordFieldVisible;

    private boolean isPasswordVisible = false; 
    

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











}
