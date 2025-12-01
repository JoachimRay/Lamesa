package main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

public class InventoryController {
    // FXML FIELDS

    @FXML
    private TableView<?> inventoryTable;

    @FXML
    private TableColumn<?, ?> checkBoxColumn;

    @FXML
    private TableColumn<?, String> productIdColumn;

    @FXML
    private TableColumn<?, String> productColumn;

    @FXML
    private TableColumn<?, String> categoryColumn;

    @FXML
    private TableColumn<?, String> typeColumn;

    @FXML
    private TableColumn<?, String> instructionColumn;

    @FXML
    private TableColumn<?, String> itemsColumn;

    @FXML
    private TableColumn<?, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button newStockButton;

    @FXML
    private Button statusFilterButton;

    // Initializing

    @FXML
    public void initialize() {
        // Called automatically after FXML loads
        System.out.println("Inventory page loaded!");
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
