package main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * EmployeesController
 *
 * This controller manages the Employees page UI.
 *
 * Features:
 * - Displays a list of users from the database
 * - Shows username, role, last login, and shift status
 * - Search bar to filter employees by username
 * - Role filter dropdown (All, Admin, Employee)
 */
public class EmployeesController {

    // TableView and columns mapped from FXML
    @FXML private TableView<Employee> employeesTable;
    @FXML private TableColumn<Employee, String> usernameColumn;
    @FXML private TableColumn<Employee, String> roleColumn;
    @FXML private TableColumn<Employee, String> lastLoginColumn;
    @FXML private TableColumn<Employee, String> shiftStatusColumn;

    // Search field and filter dropdown
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;

    // ObservableList stores employee data for the TableView
    private final ObservableList<Employee> data = FXCollections.observableArrayList();
    private ObservableList<Employee> filteredData = FXCollections.observableArrayList();

    // Formatter to parse and display timestamps
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // DAO object to fetch data from DB
    private final EmployeeDAO dao = new EmployeeDAO();

    @FXML
    private void initialize() {
        System.out.println("[EmployeesController] Initializing...");
        
        // 1) Configure table columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        shiftStatusColumn.setCellValueFactory(new PropertyValueFactory<>("shiftStatus"));

        // 2) Make username column bold
        usernameColumn.setCellFactory(column -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #114F3A;");
                }
            }
        });

        // 3) Setup role filter ComboBox with custom cell factory
        roleFilterCombo.setItems(FXCollections.observableArrayList("All", "manager", "employee"));
        roleFilterCombo.setValue("All");
        roleFilterCombo.setCellFactory(column -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #333 !important; -fx-background-color: white; -fx-padding: 10px;");
                }
            }
        });
        roleFilterCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white;");
                }
            }
        });
        roleFilterCombo.setOnAction(e -> filterEmployees());

        // 4) Load data from database
        Platform.runLater(() -> {
            loadData();
            filterEmployees();
        });

        // 5) Setup search/filter feature
        setupSearch();
        
        System.out.println("[EmployeesController] Initialization complete. Total employees: " + data.size());
    }

    /**
     * Load all users and their last attendance from the database.
     * Populates the ObservableList for the TableView.
     */
    private void loadData() {
        System.out.println("[EmployeesController] Loading data from database...");
        data.clear();

        try {
            // Fetch all users with last login/logout info
            List<EmployeeDAO.EmployeeRow> rows = dao.fetchAllUsersWithLastAttendance();
            System.out.println("[EmployeesController] Fetched " + rows.size() + " users from database");

            for (EmployeeDAO.EmployeeRow row : rows) {
                String username = row.username;
                String role = row.role != null ? row.role : "Employee"; // default role
                String lastLogin = row.lastLogin != null ? row.lastLogin : "Never logged in";
                String lastLogout = row.lastLogout;

                // Determine shift status (Completed, hours worked, or "-")
                String shiftStatus = computeShiftStatus(row.lastLogin, lastLogout);

                System.out.println("[EmployeesController] Adding: " + username + " | " + role + " | " + lastLogin + " | " + shiftStatus);

                // Add to ObservableList
                data.add(new Employee(username, role, lastLogin, shiftStatus));
            }

            System.out.println("[EmployeesController] Data loaded successfully. Total items: " + data.size());
            
        } catch (Exception e) {
            System.err.println("[EmployeesController] ERROR loading data:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load employee data: " + e.getMessage());
        }
    }

    /**
     * Filter employees by role and search
     */
    private void filterEmployees() {
        String roleFilter = roleFilterCombo.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        filteredData.clear();

        for (Employee e : data) {
            boolean matchesRole = roleFilter.equals("All") || e.getRole().equals(roleFilter);
            boolean matchesSearch = searchText.isEmpty() || e.getUsername().toLowerCase().contains(searchText);

            if (matchesRole && matchesSearch) {
                filteredData.add(e);
            }
        }

        employeesTable.setItems(filteredData);
        System.out.println("[EmployeesController] Filtered: " + filteredData.size() + " employees shown (Role: " + roleFilter + ", Search: " + searchText + ")");
    }

    /**
     * Computes shift status text for display in TableView.
     *
     * @param loginTs  last login timestamp
     * @param logoutTs last logout timestamp
     * @return "Completed" if >=10 seconds, otherwise "X hrs", "Active", or "No data"
     */
    private String computeShiftStatus(String loginTs, String logoutTs) {
        if (loginTs == null || loginTs.equals("Never logged in")) {
            return "No data";
        }
        
        if (logoutTs == null) {
            return "Active"; // Currently logged in
        }
        
        try {
            LocalDateTime login = LocalDateTime.parse(loginTs, TF);
            LocalDateTime logout = LocalDateTime.parse(logoutTs, TF);
            long seconds = Duration.between(login, logout).getSeconds();
            
            if (seconds >= 10) {
                return "✓ Completed"; // Shift completed (>=10 seconds)
            } else {
                double hours = seconds / 3600.0;
                return String.format("%.2f hrs", hours);
            }
        } catch (DateTimeParseException e) {
            System.err.println("[EmployeesController] Date parse error: " + e.getMessage());
            return "-";
        }
    }

    /**
     * Setup search/filter for username.
     * Filters the TableView based on searchField text.
     */
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterEmployees();
        });
    }

    /**
     * Simple helper to show alerts.
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    /**
     * Employee model for TableView rows.
     * Make sure getters match PropertyValueFactory column names.
     */
    public static class Employee {
        private final String username;
        private final String role;
        private final String lastLogin;
        private final String shiftStatus;

        public Employee(String username, String role, String lastLogin, String shiftStatus) {
            this.username = username;
            this.role = role;
            this.lastLogin = lastLogin;
            this.shiftStatus = shiftStatus;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getLastLogin() { return lastLogin; }
        public String getShiftStatus() { return shiftStatus; }
    }
}