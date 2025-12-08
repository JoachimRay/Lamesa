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
        instructionCombo.setItems(FXCollections.observableArrayList("Out of Stock", "Low in Stock", "High in Stock"));
        statusCombo.setItems(FXCollections.observableArrayList("No Action Required", "Pending", "Completed"));
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
        String instruction = instructionCombo.getValue();
        String status = statusCombo.getValue();
        int stock = Integer.parseInt(stockField.getText());

        String category = "";
        String type = "";
        String dbUrl = "jdbc:sqlite:database/lamesa.db";

        // Step 1: Get category and type from meal table
        try(Connection conn = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT category, type FROM meal WHERE name = ?";

            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, product);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    category = rs.getString("category");
                    type = rs.getString("type");
                }
            }
        } catch (SQLException e) {
            System.out.println("[NewStockDialogController] ERROR: " + e.getMessage());
        }

        // Step 2: Insert into inventory table
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String insertSQL = "INSERT INTO inventory (product_name, category, type, instruction, stock_quantity, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setString(1, product);
                ps.setString(2, category);
                ps.setString(3, type);
                ps.setString(4, instruction);
                ps.setInt(5, stock);
                ps.setString(6, status);
                ps.executeUpdate();
                System.out.println("[NewStockDialogController] Inserted: " + product);
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
