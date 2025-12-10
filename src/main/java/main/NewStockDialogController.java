package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class NewStockDialogController {
    
    @FXML
    private ComboBox<String> productNameCombo;

    @FXML
    private ComboBox<String> instructionCombo;

    @FXML
    private TextField stockField;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private Button okayButton;

    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        productNameCombo.setItems(loadProducts());
        instructionCombo.setItems(FXCollections.observableArrayList("Low in Stock", "High in Stock"));
        statusCombo.setItems(FXCollections.observableArrayList("Available", "Action Required"));
    }

    private ObservableList<String> loadProducts() {
        ObservableList<String> list = FXCollections.observableArrayList();

        String dbUrl = "jdbc:sqlite:database/lamesa.db";

        try(Connection conn = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT name FROM meal ORDER BY meal_id";

            try(PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
                
                while(rs.next()) {
                    list.add(rs.getString("name"));
                }
            }

        } catch (SQLException e) {
            System.out.println("[NewStockDialogController] ERROR: " + e.getMessage());
        }
        return list;
    }

    @FXML
    private void handleOkay() {
        String product = productNameCombo.getValue();
        String status = statusCombo.getValue();
        int stock = Integer.parseInt(stockField.getText());

        String dbUrl = "jdbc:sqlite:database/lamesa.db";

        // Step 1: Get meal_id from meal table
        int mealId = -1;
        try(Connection conn = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT meal_id FROM meal WHERE name = ?";

            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, product);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    mealId = rs.getInt("meal_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("[NewStockDialogController] ERROR getting meal_id: " + e.getMessage());
        }

        if (mealId == -1) {
            System.out.println("[NewStockDialogController] ERROR: Meal not found: " + product);
            return;
        }

        // Step 2: Insert new inventory entry (always adds new stock, not update)
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String insertSQL = "INSERT INTO inventory (meal_id, stock_quantity, status, date_added) VALUES (?, ?, ?, DATE('now'))";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setInt(1, mealId);
                ps.setInt(2, stock);
                ps.setString(3, status);
                ps.executeUpdate();
                System.out.println("[NewStockDialogController] Added new stock: " + product + " | Quantity: " + stock);
            }
        } catch (SQLException e) {
            System.out.println("[NewStockDialogController] ERROR: " + e.getMessage());
        }
        
        // Step 3: Close the dialog
        okayButton.getScene().getWindow().hide();
    }

    @FXML
    private void handleCancel() {
        cancelButton.getScene().getWindow().hide();
    }
}
