package main;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

public class InventoryController {

    // FXML FIELDS

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableColumn<?, ?> checkBoxColumn;

    @FXML
    private TableColumn<InventoryItem, String> productIdColumn;

    @FXML
    private TableColumn<InventoryItem, String> productColumn;

    @FXML
    private TableColumn<InventoryItem, String> categoryColumn;

    @FXML
    private TableColumn<InventoryItem, String> typeColumn;

    @FXML
    private TableColumn<InventoryItem, String> instructionColumn;

    @FXML
    private TableColumn<InventoryItem, Integer> stockColumn;

    @FXML
    private TableColumn<InventoryItem, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button newStockButton;

    @FXML
    private Button statusFilterButton;
    
    // Initializing the columns which getter to use from InventoryItem.java

    @FXML
    public void initialize() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        instructionColumn.setCellValueFactory(new PropertyValueFactory<>("instruction"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));


        inventoryTable.setEditable(true);
        stockColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        loadInventoryData();
    }   

    private void loadInventoryData() {
            ObservableList<InventoryItem> items = FXCollections.observableArrayList();
            
            String dbUrl = "jdbc:sqlite:database/lamesa.db";
            System.out.println("[InventoryController] Connecting to: " + dbUrl);
            try(Connection conn = DriverManager.getConnection(dbUrl)) {             
                System.out.println("[InventoryController] Connected successfully!");
            
                String sql = "SELECT * FROM inventory";
                try(PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                    
                        // Loop through each row
                        while (rs.next()) {
                            // Get each
                            int id = rs.getInt("product_id");
                            String productName = rs.getString("product_name");
                            String category = rs.getString("category");
                            String type = rs.getString("type");
                            String instruction = rs.getString("instruction");
                            int stockQuantity = rs.getInt("stock_quantity");
                            String status = rs.getString("status");

                            InventoryItem item = new InventoryItem(id, productName, category, type, instruction, stockQuantity, status);
                            items.add(item);
                        }
                }
            } catch (SQLException e) {
                System.out.println("[InventoryController] ERROR: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("[InventoryController] Total items loaded: " + items.size());
            inventoryTable.setItems(items);
        }

    // Event handling

    @FXML
    private void handleNewStock() {
        System.out.println("New Stock button clicked");
        // Opens a dialog to add a new item
    }

    @FXML
    private void handleStatusFilter() {
    }
}
