package main;

// Java SQL imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;



// Controller for the Inventory page.
// Handles displaying, editing, filtering, and searching inventory items.

public class InventoryController {

    // ==================== FXML FIELDS - TABLE ====================

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableColumn<?, ?> checkBoxColumn;

    @FXML
    private TableColumn<InventoryItem, Integer> productIdColumn;

    @FXML
    private TableColumn<InventoryItem, String> productColumn;

    @FXML
    private TableColumn<InventoryItem, String> categoryColumn;

    @FXML
    private TableColumn<InventoryItem, String> typeColumn;



    @FXML
    private TableColumn<InventoryItem, Integer> stockColumn;

    @FXML
    private TableColumn<InventoryItem, String> statusColumn;

    // ==================== FXML FIELDS - CONTROLS ====================

    @FXML
    private TextField searchField;

    @FXML
    private Button newStockButton;

    @FXML
    private javafx.scene.control.ComboBox<String> statusFilterCombo;

    // ==================== DATA STORAGE ====================

    // Master list containing all inventory items from database
    private ObservableList<InventoryItem> masterObservableList;

    // ==================== INITIALIZATION ====================

    
    // Initializes the controller.
    // Sets up table columns, loads data, and configures editable cells.
    
    @FXML
    public void initialize() {
        try {
            System.out.println("[InventoryController] Initialize starting...");
            
            productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Make product name bold
            productColumn.setCellFactory(column -> new javafx.scene.control.TableCell<InventoryItem, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-font-weight: bold; -fx-padding: 8; -fx-alignment: center;");
                    }
                }
            });

            System.out.println("[InventoryController] Cell value factories set");

            // Setup status filter ComboBox
            statusFilterCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                "All", "Available", "Low Stock", "Out of Stock"
            ));
            statusFilterCombo.setValue("All");
            statusFilterCombo.setStyle("-fx-text-fill: white; -fx-background-color: #228866;");
            
            // Custom cell factory for dropdown items with dark text
            statusFilterCombo.setCellFactory(param -> new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #333; -fx-background-color: white; -fx-padding: 10 12 10 12; -fx-font-size: 12;");
                    }
                }
            });
            
            statusFilterCombo.setOnAction(event -> handleStatusFilterCombo());

            // Delay loading data to ensure UI is ready
            javafx.application.Platform.runLater(() -> {
                System.out.println("[InventoryController] Loading inventory data...");
                loadInventoryData();
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
                item.setStockQuantity(newStock);
            });

            // Makes the column be able to choose No Action Required, Pending and Completed
            statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn("No Action Required","Pending", "Completed"));
            statusColumn.setOnEditCommit(event -> {
                String newStatus = event.getNewValue();
                InventoryItem item = event.getRowValue();
                int product_id = item.getId();

                updateStatusInDatabase(newStatus, product_id);
                System.out.println("Update product " + product_id + " to status: " + newStatus);
                item.setStatus(newStatus);
            });

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterInventory(newValue);
            });
            
            System.out.println("[InventoryController] Initialize complete");
        } catch (Exception e) {
            System.out.println("[InventoryController] ERROR in initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== DATA LOADING ====================

    // Loads all inventory items from the database into the table.
    private void loadInventoryData() {
        System.out.println("[InventoryController] loadInventoryData() called");
        
        if (inventoryTable == null) {
            System.out.println("[InventoryController] ERROR: inventoryTable is null!");
            return;
        }
        
        masterObservableList = FXCollections.observableArrayList();
        
        String dbUrl = "jdbc:sqlite:database/lamesa.db";
        System.out.println("[InventoryController] Connecting to: " + dbUrl);
        try(Connection conn = DriverManager.getConnection(dbUrl)) {             
            System.out.println("[InventoryController] Connected successfully!");
            
            // FIX: Update meal records to use correct category_id and type_id based on dish type
            System.out.println("[InventoryController] FIXING: Updating meal records with appropriate categories and types");
            String fixSql = "UPDATE meal SET category_id = CASE meal_id " +
                           "WHEN 1 THEN 8 " +  // Chicken Adobo - Non-Vegetarian
                           "WHEN 2 THEN 8 " +  // Pork Sinigang - Non-Vegetarian
                           "WHEN 3 THEN 8 " +  // Beef Kare-Kare - Non-Vegetarian
                           "WHEN 4 THEN 8 " +  // Lechon Kawali - Non-Vegetarian
                           "WHEN 5 THEN 8 " +  // Pancit Canton - Non-Vegetarian
                           "WHEN 6 THEN 8 " +  // Pork Sisig - Non-Vegetarian
                           "WHEN 7 THEN 8 " +  // Bulalo - Non-Vegetarian
                           "WHEN 8 THEN 8 " +  // Tinola - Non-Vegetarian
                           "WHEN 9 THEN 8 " +  // Laing - Non-Vegetarian
                           "WHEN 10 THEN 8 " + // Bicol Express - Non-Vegetarian
                           "WHEN 11 THEN 8 " + // Fried Fish - Non-Vegetarian
                           "END, " +
                           "type_id = CASE meal_id " +
                           "WHEN 1 THEN 8 " +  // Chicken Adobo - Lunch
                           "WHEN 2 THEN 8 " +  // Pork Sinigang - Lunch
                           "WHEN 3 THEN 8 " +  // Beef Kare-Kare - Lunch
                           "WHEN 4 THEN 8 " +  // Lechon Kawali - Lunch
                           "WHEN 5 THEN 8 " +  // Pancit Canton - Lunch
                           "WHEN 6 THEN 8 " +  // Pork Sisig - Lunch (appetizer)
                           "WHEN 7 THEN 8 " +  // Bulalo - Lunch
                           "WHEN 8 THEN 8 " +  // Tinola - Lunch
                           "WHEN 9 THEN 8 " +  // Laing - Lunch
                           "WHEN 10 THEN 8 " + // Bicol Express - Lunch
                           "WHEN 11 THEN 8 " + // Fried Fish - Lunch
                           "END";
            try(PreparedStatement fixPs = conn.prepareStatement(fixSql)) {
                int updated = fixPs.executeUpdate();
                System.out.println("[InventoryController] FIXED: Updated " + updated + " meal records");
            }
            
            // Query with category and type joins
            String sql = "SELECT i.inventory_id, m.name as product_name, mc.category_name, mt.type_name, " +
                         "i.stock_quantity, i.status " +
                         "FROM inventory i " +
                         "LEFT JOIN meal m ON i.meal_id = m.meal_id " +
                         "LEFT JOIN meal_category mc ON m.category_id = mc.category_id " +
                         "LEFT JOIN meal_types mt ON m.type_id = mt.type_id";
            
            System.out.println("[InventoryController] Executing query: " + sql);
            
            try(PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    int id = rs.getInt("inventory_id");
                    String productName = rs.getString("product_name");
                    String categoryName = rs.getString("category_name");
                    String typeName = rs.getString("type_name");
                    int stockQuantity = rs.getInt("stock_quantity");
                    String status = rs.getString("status");
                    
                    count++;
                    System.out.println("[InventoryController] Row " + count + ": ID=" + id + ", Name=" + productName + ", Category=" + categoryName + ", Type=" + typeName + ", Stock=" + stockQuantity + ", Status=" + status);
                    
                    // Create InventoryItem with meal data
                    InventoryItem item = new InventoryItem(id, productName != null ? productName : "Unknown", categoryName != null ? categoryName : "", typeName != null ? typeName : "", "", stockQuantity, status);
                    masterObservableList.add(item);
                }
                System.out.println("[InventoryController] Total items loaded: " + count);
            }
            inventoryTable.setItems(masterObservableList);
            System.out.println("[InventoryController] Table items set");
        } catch (SQLException e) {
            System.out.println("[InventoryController] SQL ERROR: " + e.getMessage());
            System.out.println("[InventoryController] SQL State: " + e.getSQLState());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("[InventoryController] General ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Refreshes the inventory data from the database
    public void refreshInventoryData() {
        System.out.println("[InventoryController] Refreshing inventory data...");
        loadInventoryData();
        filterInventory(searchField.getText());
    }

    // ==================== DATABASE UPDATE METHODS ====================


    // Updates the instruction field in the database.
   


 
    // Updates the stock quantity in the database.

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

    
    // Updates the status field in the database.
    
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

    // ==================== FILTER METHODS ====================

    
    // Filters the inventory table by product name.
   
    private void filterInventory(String searchText) {

        if(searchText.isEmpty()) {
            inventoryTable.setItems(masterObservableList);
        }
        else {
            ObservableList<InventoryItem> filteredList = FXCollections.observableArrayList();

            for(InventoryItem item : masterObservableList)
                if (item.getProductName().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(item);
                }
                inventoryTable.setItems(filteredList);
        }
    }

    // ==================== EVENT HANDLERS ====================

    
     //Handles the "New Stock" button click.
     // Opens a dialog to add a new inventory item using FXML.
    
    @FXML
    private void handleNewStock() {
        System.out.println("New Stock button clicked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/newstock-dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Stock");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh the table after dialog closes
            refreshInventoryData();
        } catch (Exception e) {
            System.out.println("[InventoryController] ERROR loading dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("[InventoryController] Refresh button clicked");
        refreshInventoryData();
    }

     // Handles the status filter button click.
     // Cycles through: All -> No Action Required -> Pending -> Completed -> All...


    @FXML
    private void handleStatusFilter() {
        
        String currentFilter = statusFilterCombo.getValue();

        if(currentFilter.equals("All")) {
            inventoryTable.setItems(masterObservableList);
        }
        else {
            ObservableList<InventoryItem> filteredList = FXCollections.observableArrayList();

            for(InventoryItem item : masterObservableList) {
                boolean matches = false;
                
                if (currentFilter.equals("Available") && item.getStockQuantity() > 10) {
                    matches = true;
                } else if (currentFilter.equals("Low Stock") && item.getStockQuantity() > 0 && item.getStockQuantity() <= 10) {
                    matches = true;
                } else if (currentFilter.equals("Out of Stock") && item.getStockQuantity() == 0) {
                    matches = true;
                }
                
                if (matches) {
                    filteredList.add(item);
                }
            }
            inventoryTable.setItems(filteredList);
        }

        System.out.println("[InventoryController] Status filter: " + currentFilter);
    }
    
    private void handleStatusFilterCombo() {
        handleStatusFilter();
    }
}
