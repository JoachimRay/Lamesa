package main;

import javafx.fxml.FXML;
import java.sql.*;

/**
 * ChoiceController
 * 
 * Handles role selection after user registration.
 * Updates the most recently created user's role to either 'manager' or 'employee',
 * then navigates back to the login screen.
 * 
 * FIXED ISSUES:
 * - Changed database path from "Database/lamesa.db" to "database/lamesa.db" (lowercase)
 */
public class ChoiceController {

    // SQL queries to update the most recent user's role
    private String manager = "UPDATE users SET role = 'manager' WHERE id = (SELECT MAX(id) FROM users)";
    private String employee = "UPDATE users SET role = 'employee' WHERE id = (SELECT MAX(id) FROM users)"; 
    
    // FIXED: Changed to lowercase "database/lamesa.db" for consistency
    private String sqldb = "jdbc:sqlite:database/lamesa.db";

    /**
     * Handles Manager button click.
     * Sets the most recent user's role to 'manager' and navigates to login.
     */
    @FXML
    private void handleManager(){
        try { 
            Class.forName("org.sqlite.JDBC");
            try(Connection conn = DriverManager.getConnection(sqldb); 
                PreparedStatement ps = conn.prepareStatement(manager)) { 
                ps.executeUpdate();
                System.out.println("[ChoiceController] Manager role assigned");
                App.setRoot("login"); 
            }
        } catch (SQLException e) { 
            System.err.println("[ChoiceController] SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) { 
            System.err.println("[ChoiceController] Class Not Found: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles Employee button click.
     * Sets the most recent user's role to 'employee' and navigates to login.
     */
    @FXML
    private void handleEmployee(){ 
        try{ 
            Class.forName("org.sqlite.JDBC");
            try(Connection conn = DriverManager.getConnection(sqldb);
                PreparedStatement ps = conn.prepareStatement(employee)) { 
                ps.executeUpdate();
                System.out.println("[ChoiceController] Employee role assigned"); 
                App.setRoot("login"); 
            }
        } catch (SQLException e) { 
            System.err.println("[ChoiceController] SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) { 
            System.err.println("[ChoiceController] Class Not Found: " + e.getMessage());
            e.printStackTrace();
        }
    }
}