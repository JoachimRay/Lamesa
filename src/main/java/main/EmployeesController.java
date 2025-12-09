package main;

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
 * 
 * FIXED: Added better error handling and debug logging
 */
public class EmployeesController {

    // TableView and columns mapped from FXML
    @FXML private TableView<Employee> employeesTable;
    @FXML private TableColumn<Employee, String> usernameColumn;
    @FXML private TableColumn<Employee, String> roleColumn;
    @FXML private TableColumn<Employee, String> lastLoginColumn;
    @FXML private TableColumn<Employee, String> shiftStatusColumn;

    // Search field for filtering employees
    @FXML private TextField searchField;

    // ObservableList stores employee data for the TableView
    private final ObservableList<Employee> data = FXCollections.observableArrayList();

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

        // 2) Load data from database
        loadData();

        // 3) Setup search/filter feature
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
                String role = row.role != null ? row.role : "employee"; // default role
                String lastLogin = row.lastLogin != null ? row.lastLogin : "Never logged in";
                String lastLogout = row.lastLogout;

                // Determine shift status (Completed, hours worked, or "-")
                String shiftStatus = computeShiftStatus(row.lastLogin, lastLogout);

                System.out.println("[EmployeesController] Adding: " + username + " | " + role + " | " + lastLogin + " | " + shiftStatus);

                // Add to TableView
                data.add(new Employee(username, role, lastLogin, shiftStatus));
            }

            employeesTable.setItems(data);
            System.out.println("[EmployeesController] Data loaded successfully. Table has " + data.size() + " items");
            
        } catch (Exception e) {
            System.err.println("[EmployeesController] ERROR loading data:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load employee data: " + e.getMessage());
        }
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
            String filter = newVal == null ? "" : newVal.toLowerCase().trim();
            if (filter.isEmpty()) {
                employeesTable.setItems(data);
            } else {
                ObservableList<Employee> filtered = FXCollections.observableArrayList();
                for (Employee e : data) {
                    if (e.getUsername().toLowerCase().contains(filter)) {
                        filtered.add(e);
                    }
                }
                employeesTable.setItems(filtered);
            }
        });
    }

    /**
     * Refresh button handler - reloads data from database
     */
    @FXML
    private void handleRefresh() {
        System.out.println("[EmployeesController] Refresh button clicked");
        loadData();
        showAlert(Alert.AlertType.INFORMATION, "Refreshed", "Employee data has been refreshed");
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