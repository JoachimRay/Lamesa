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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;



// Controller for the Inventory page.
// Handles displaying, editing, filtering, and searching inventory items.

public class InventoryController 
{

    // ==================== FXML FIELDS - TABLE ====================

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableColumn<InventoryItem, Boolean> checkBoxColumn;

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
    private TableColumn<InventoryItem, String> dateAddedColumn;

    // ==================== FXML FIELDS - CONTROLS ====================

    @FXML
    private TextField searchField;

    @FXML
    private Button newStockButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button statusFilterButton;

    // Checkbox for "Select All" in header
    private CheckBox selectAllCheckBox;

    // ==================== DATA STORAGE ====================

    // Master list containing all inventory items from database
    private ObservableList<InventoryItem> masterObservableList;

    // Index for cycling through status filters
    private int statusFilterIndex = 0;

    // Available status filter options
    private String[] statusFilters = {"All", "Available", "Action Required"};

    // ==================== INITIALIZATION ====================

    
    // Initializes the controller.
    // Sets up table columns, loads data, and configures editable cells.
    
    @FXML
    public void initialize() 
    {
        // Setup checkbox column with header checkbox for "Select All"
        selectAllCheckBox = new CheckBox();
        selectAllCheckBox.setStyle("-fx-font-size: 12; -fx-padding: 0;");
        checkBoxColumn.setGraphic(selectAllCheckBox);
        selectAllCheckBox.setOnAction(event -> 
        {
            boolean selectAll = selectAllCheckBox.isSelected();
            for (InventoryItem item : inventoryTable.getItems()) 
            {
                item.setSelected(selectAll);
            }
            inventoryTable.refresh();
        });

        // Setup checkbox cells for each row (multi-select enabled)
        checkBoxColumn.setCellFactory(column -> new TableCell<InventoryItem, Boolean>() 
        {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> 
                {
                    InventoryItem currentItem = getTableView().getItems().get(getIndex());
                    currentItem.setSelected(checkBox.isSelected());
                    getTableView().refresh();
                });
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) 
            {
                super.updateItem(item, empty);
                if (empty) 
                {
                    setGraphic(null);
                } 
                else 
                {
                    InventoryItem inventoryItem = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(inventoryItem.isSelected());
                    setGraphic(checkBox);
                }
            }
        });

        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        instructionColumn.setCellValueFactory(new PropertyValueFactory<>("instruction"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateAddedColumn.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));

        // Loads the data
        loadInventoryData();

        // Instruction column is read-only (auto-calculated based on stock)

        // Makes the table editable on stock
        inventoryTable.setEditable(true);
        stockColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        stockColumn.setOnEditCommit(event -> 
        {
            int newStock = event.getNewValue();
            InventoryItem item = event.getRowValue();
            int inventoryId = item.getId();

            // Update stock in database
            updateStockInDatabase(newStock, inventoryId);
            System.out.println("Update inventory " + inventoryId + " to stock: " + newStock);
            item.setStockQuantity(newStock);

            // Auto-update instruction and status based on stock level
            if (newStock < 10) 
            {
                item.setInstruction("Low in Stock");
                item.setStatus("Action Required");
                
                // Show alert for low stock
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Low Stock Warning");
                alert.setHeaderText("Stock is running low!");
                alert.setContentText(item.getProductName() + " has only " + newStock + " items left.\n\nAction Required!");
                alert.showAndWait();
            } 
            else 
            {
                item.setInstruction("High in Stock");
                item.setStatus("Available");
            }
            
            // Refresh table to show updated values
            inventoryTable.refresh();
        });

        // Status column is read-only (managed automatically based on stock)

        searchField.textProperty().addListener((observable, oldValue, newValue) -> 
        {
            filterInventory(newValue);
        });
    }

    // ==================== DATA LOADING ====================


    // Loads all inventory items from the database into the table.

    private void loadInventoryData() 
    {
        masterObservableList = FXCollections.observableArrayList();
        
        // Connection to database
        String dbUrl = "jdbc:sqlite:database/lamesa.db";
        System.out.println("[InventoryController] Connecting to: " + dbUrl);
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) 
        {             
            System.out.println("[InventoryController] Connected successfully!");
            
            // JOIN with meal, meal_category, and meal_types to get names
            String sql = "SELECT i.inventory_id, m.name AS product_name, " +
                         "mc.category_name AS category, mt.type_name AS type, " +
                         "i.stock_quantity, i.status, i.date_added " +
                         "FROM inventory i " +
                         "JOIN meal m ON i.meal_id = m.meal_id " +
                         "LEFT JOIN meal_category mc ON m.category_id = mc.category_id " +
                         "LEFT JOIN meal_types mt ON m.type_id = mt.type_id " +
                         "ORDER BY m.name";
            
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) 
            {
                // Loop through each row
                while (rs.next()) 
                {
                    // Get each
                    int id = rs.getInt("inventory_id");
                    String productName = rs.getString("product_name");
                    String category = rs.getString("category");
                    String type = rs.getString("type");
                    int stockQuantity = rs.getInt("stock_quantity");
                    String dateAdded = rs.getString("date_added");
                    
                    // Auto-calculate instruction and status based on stock
                    String instruction;
                    String status;
                    if (stockQuantity <= 10) 
                    {
                        instruction = "Low in Stock";
                        status = "Action Required";
                    } 
                    else 
                    {
                        instruction = "High in Stock";
                        status = "Available";
                    }
                    
                    // POJO 
                    InventoryItem item = new InventoryItem(id, productName, category, type, instruction, stockQuantity, status, dateAdded);
                    masterObservableList.add(item);
                }
            }
        } 
        catch (SQLException e) 
        {
            System.out.println("[InventoryController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[InventoryController] Total items loaded: " + masterObservableList.size());
        System.out.println("[InventoryController] Master Observable List loaded: " + masterObservableList.size());
        inventoryTable.setItems(masterObservableList);
    }

    // ==================== DATABASE UPDATE METHODS ====================


    // Updates the instruction field in the database.
   
    private void updateInstructionInDatabase(String newInstruction, int inventoryId) 
    {
        // Note: instruction column doesn't exist in meal-linked inventory
        // This is kept for UI compatibility but does nothing
        System.out.println("[InventoryController] Instruction update skipped (not stored in DB)");
    }

 
    // Updates the stock quantity in the database.

    private void updateStockInDatabase(int newStock, int inventoryId) 
    {
        String dburl = "jdbc:sqlite:database/lamesa.db";
        
        try (Connection con = DriverManager.getConnection(dburl)) 
        {
            String sql = "UPDATE inventory SET stock_quantity = ? WHERE inventory_id = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) 
            {
                ps.setInt(1, newStock);
                ps.setInt(2, inventoryId);
                ps.executeUpdate();
            }
        } 
        catch (SQLException e) 
        {
            System.out.println("[InventoryController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    // Updates the status field in the database.
    
    private void updateStatusInDatabase(String newStatus, int inventoryId) 
    {
        String dburl = "jdbc:sqlite:database/lamesa.db";
        
        try (Connection con = DriverManager.getConnection(dburl)) 
        {
            String sql = "UPDATE inventory SET status = ? WHERE inventory_id = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) 
            {
                ps.setString(1, newStatus);
                ps.setInt(2, inventoryId);
                ps.executeUpdate();
            }
        } 
        catch (SQLException e) 
        {
            System.out.println("[InventoryController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== FILTER METHODS ====================

    
    // Filters the inventory table by product name.
   
    private void filterInventory(String searchText) 
    {
        if (searchText.isEmpty()) 
        {
            inventoryTable.setItems(masterObservableList);
        }
        else 
        {
            ObservableList<InventoryItem> filteredList = FXCollections.observableArrayList();

            for (InventoryItem item : masterObservableList)
            {
                if (item.getProductName().toLowerCase().contains(searchText.toLowerCase())) 
                {
                    filteredList.add(item);
                }
            }
            inventoryTable.setItems(filteredList);
        }
    }

    // ==================== EVENT HANDLERS ====================

    
    // Handles the "New Stock" button click.
    // Opens a dialog to add/update stock for a meal.
    
    @FXML
    private void handleNewStock() 
    {
        System.out.println("New Stock button clicked");
        
        try 
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/newstock-dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add/Update Stock");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh the table after dialog closes
            loadInventoryData();
        } 
        catch (Exception e) 
        {
            System.out.println("[InventoryController] ERROR loading dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handles the status filter button click.
    // Cycles through: All -> No Action Required -> Pending -> Completed -> All...

    @FXML
    private void handleStatusFilter() 
    {
        String currentFilter = statusFilters[statusFilterIndex];

        if (currentFilter.equals("All")) 
        {
            inventoryTable.setItems(masterObservableList);
        }
        else 
        {
            ObservableList<InventoryItem> filteredList = FXCollections.observableArrayList();

            for (InventoryItem item : masterObservableList) 
            {
                if (item.getStatus().toLowerCase().equals(currentFilter.toLowerCase())) 
                {
                    filteredList.add(item);
                }
            }
            inventoryTable.setItems(filteredList);
        }

        System.out.println("[InventoryController] current status: " + currentFilter);
        ++statusFilterIndex;
        statusFilterButton.setText(currentFilter);
        
        if (statusFilterIndex >= statusFilters.length)
            statusFilterIndex = 0;
    }

    // Handles delete button click - deletes selected items
    @FXML
    private void handleDelete() 
    {
        // Collect selected items
        ObservableList<InventoryItem> toDelete = FXCollections.observableArrayList();
        
        for (InventoryItem item : inventoryTable.getItems()) 
        {
            if (item.isSelected()) 
            {
                toDelete.add(item);
            }
        }

        if (toDelete.isEmpty()) 
        {
            System.out.println("[InventoryController] No items selected for deletion");
            return;
        }

        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Inventory");
        alert.setHeaderText("Delete " + toDelete.size() + " item(s)?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) 
        {
            String dbUrl = "jdbc:sqlite:database/lamesa.db";
            
            try (Connection conn = DriverManager.getConnection(dbUrl)) 
            {
                String sql = "DELETE FROM inventory WHERE inventory_id = ?";
                
                try (PreparedStatement ps = conn.prepareStatement(sql)) 
                {
                    for (InventoryItem item : toDelete) 
                    {
                        ps.setInt(1, item.getId());
                        ps.executeUpdate();
                        System.out.println("[InventoryController] Deleted: " + item.getProductName());
                    }
                }
            } 
            catch (SQLException e) 
            {
                System.out.println("[InventoryController] ERROR deleting: " + e.getMessage());
                e.printStackTrace();
            }

            // Reset select all checkbox and refresh
            selectAllCheckBox.setSelected(false);
            loadInventoryData();
        }
    }
}
