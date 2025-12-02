package main;

import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
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
import javafx.scene.control.cell.ComboBoxTableCell;
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

        // Loads the data
        loadInventoryData();

        // Makes the table be able to choose Out of Stock, Low in Stock, and High in Stock

        instructionColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Out of Stock", "Low in Stock", "High in Stock"));
        instructionColumn.setOnEditCommit(event ->{
            String newInstruction = event.getNewValue();
            InventoryItem item = event.getRowValue();
            int product_id = item.getId();

            updateInstructionInDatabase(newInstruction, product_id);
            System.out.println("Update product " + product_id + " to instruction: " + newInstruction);
        });

        // Makes the table editable on stock

        inventoryTable.setEditable(true);
        stockColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        stockColumn.setOnEditCommit(event -> {
            int newStock = event.getNewValue();
            InventoryItem item = event.getRowValue();
            int product_id = item.getId();

            updateStockInDatabase(newStock, product_id);
            System.out.println("Update product " + product_id + " to stock: " + newStock);

        });

        // Makes the column be able to choose Pending and Completed

        inventoryTable.setEditable(true);
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn("No Action Required","Pending", "Completed"));
        statusColumn.setOnEditCommit(event -> {
            String newStatus = event.getNewValue();
            InventoryItem item = event.getRowValue();
            int product_id = item.getId();

            updateStatusInDatabase(newStatus, product_id);
            System.out.println("Update product " + product_id + " to status: " + newStatus);
        });
    }   

    // Loading Inventory Table to Table view by connecting to Lamesa Database and using Obsevable List
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
    
    // Action listener for updating Instruction in Database
    private void updateInstructionInDatabase(String newInstruction, int product_id) {

        String dburl = "jdbc:sqlite:database/lamesa.db";
        try(Connection con = DriverManager.getConnection(dburl)) {
            String sql = "UPDATE inventory SET instruction = ? WHERE product_id = ?";
            
            try(PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, newInstruction);
                ps.setInt(2, product_id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
                System.out.println("[InventoryController] ERROR: " + e.getMessage());
                e.printStackTrace();
        }
    }
    // Action listener for updating stock in Database
    private void updateStockInDatabase(int newStock, int product_id) {
        
        String dburl = "jdbc:sqlite:database/lamesa.db";
        try(Connection con = DriverManager.getConnection(dburl)) {
            String sql = "UPDATE inventory SET stock_quantity = ? WHERE product_id = ?";

            try(PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, newStock);
                ps.setInt(2, product_id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("[InventoryController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatusInDatabase(String newStatus, int product_id) {
        
        String dburl = "jdbc:sqlite:database/lamesa.db";
        try(Connection con = DriverManager.getConnection(dburl)) {
            String sql = "UPDATE inventory SET status = ? WHERE product_id = ?";

            try(PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, newStatus);
                ps.setInt(2, product_id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
                System.out.println("[InventoryController] ERROR: " + e.getMessage());
                e.printStackTrace();
        }
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
