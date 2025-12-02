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



public class ChoiceController {

    private String manager = "UPDATE users SET role = 'manager' WHERE id = (SELECT MAX(id) FROM users )";
    private String employee = "UPDATE users SET role = 'employee' WHERE id = (SELECT MAX(id) FROM users )"; 
    private String sqldb = "jdbc:sqlite:Database/lamesa.db";

    @FXML
    private void handleManager(){

        try { 
            Class.forName("org.sqlite.JDBC");
            try(Connection conn = DriverManager.getConnection(sqldb); 
        PreparedStatement ps = conn.prepareStatement(manager)) { 
                ps.executeUpdate();
                System.out.println("Manager role assigned");
                App.setRoot("login"); 
        }
    } catch (SQLException e) { 
        System.err.println("[ChoiceController] SQL Error: " + e.getMessage());
    } catch (ClassNotFoundException e) { 
        System.err.println("[ChoiceController] Class Not Found: " + e.getMessage());   
    }
}

    @FXML
    private void handleEmployee(){ 
        try{ 
            Class.forName("org.sqlite.JDBC");
            try(Connection conn = DriverManager.getConnection(sqldb);
        PreparedStatement ps = conn.prepareStatement(employee)) { 
                ps.executeUpdate();
                System.out.println("Employee role assigned"); 
                App.setRoot("login"); 
        }
        }catch (SQLException e) { 
            System.err.println("[ChoiceController] SQL Error: " + e.getMessage());
        } catch (ClassNotFoundException e) { 
            System.err.println("[ChoiceController] Class Not Found: " + e.getMessage());   
        }
    }

}
