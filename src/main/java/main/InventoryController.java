package main;

// Java SQL imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// JavaFX imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;


/**
 * Controller for the Inventory page.
 * Handles displaying, editing, filtering, and searching inventory items.
 */
public class InventoryController {

    // ==================== FXML FIELDS - TABLE ====================

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

    // ==================== FXML FIELDS - CONTROLS ====================

    @FXML
    private TextField searchField;

    @FXML
    private Button newStockButton;

    @FXML
    private Button statusFilterButton;

    // ==================== DATA STORAGE ====================

    /** Master list containing all inventory items from database */
    private ObservableList<InventoryItem> masterObservableList;

    /** Index for cycling through status filters */
    private int statusFilterIndex = 0;

    /** Available status filter options */
    private String[] statusFilters = {"All", "No Action Required", "Pending", "Completed"};

    // ==================== INITIALIZATION ====================

    /**
     * Initializes the controller.
     * Sets up table columns, loads data, and configures editable cells.
     */
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
            item.setInstruction(newInstruction);
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


    }

    // ==================== DATA LOADING ====================

    /*
     * Loads all inventory items from the database into the table.
     */
    private void loadInventoryData() {
        masterObservableList = FXCollections.observableArrayList();
        
        // Connection to database
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
                        
                        // POJO 
                        InventoryItem item = new InventoryItem(id, productName, category, type, instruction, stockQuantity, status);
                        masterObservableList.add(item);
                    }
                }
            } catch (SQLException e) {
                System.out.println("[InventoryController] ERROR: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("[InventoryController] Total items loaded: " + masterObservableList.size());
            System.out.println("[InventoryController] Master Observable List loaded: " + masterObservableList.size());
            inventoryTable.setItems(masterObservableList);
        }

    // ==================== DATABASE UPDATE METHODS ====================

    /*
     * Updates the instruction field in the database.
     */
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

    /*
     * Updates the stock quantity in the database.
     */
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

    /**
     * Updates the status field in the database.
     */
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

    /**
     * Filters the inventory table by product name.
     */
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

    /**
     * Handles the "New Stock" button click.
     * Opens a dialog to add a new inventory item using FXML.
     */
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
            loadInventoryData();
        } catch (Exception e) {
            System.out.println("[InventoryController] ERROR loading dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the status filter button click.
     * Cycles through: All -> No Action Required -> Pending -> Completed -> All...
     */
    @FXML
    private void handleStatusFilter() {
        
        String currentFilter = statusFilters[statusFilterIndex];

        if(currentFilter.equals("All")) {
        inventoryTable.setItems(masterObservableList);
        }
        else {
        ObservableList<InventoryItem> filteredList = FXCollections.observableArrayList();

            for(InventoryItem item : masterObservableList) 
                if (item.getStatus().toLowerCase().equals(currentFilter.toLowerCase())) {
                    filteredList.add(item);
                }
                inventoryTable.setItems(filteredList);
        }

        System.out.println("[InventoryController] current status: " + currentFilter);
        ++statusFilterIndex;
        statusFilterButton.setText(currentFilter);
        if(statusFilterIndex >= statusFilters.length)
            statusFilterIndex = 0;
    }
}
