package main;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * MenuController - Sidebar Navigation
 * 
 * Manages all sidebar navigation buttons and RBAC (Role-Based Access Control)
 * 
 * UPDATED: Added Food Menu button and navigation
 */
public class MenuController {

    @FXML private VBox sidebarRoot;

    // Reference to all sidebar buttons
    // (Used for showing/hiding certain buttons depending on user role)
    @FXML private Button dashboardBtn;
    @FXML private Button inventoryBtn;
    @FXML private Button analyticsBtn;
    @FXML private Button foodMenuBtn;        // NEW: Food Menu button
    @FXML private Button ordersBtn;
    @FXML private Button reportsBtn;
    @FXML private Button feedbackBtn;
    @FXML private Button employeesBtn;
    @FXML private Button settingsBtn;

    /**
     * This method runs automatically when the Menu FXML is loaded.
     * We use it to:
     * 1. Set UI formatting for all menu buttons
     * 2. Apply role-based access control (RBAC)
     */
    @FXML
    private void initialize() {
        // Fixed width for the sidebar
        sidebarRoot.setPrefWidth(300);

        setButtonsContentDisplay(); // Format button layout (icon + text)
        applyRoleRestrictions();    // Hide restricted menu items
    }

    /**
     * Ensures all sidebar buttons show their icon on the left side of the text.
     */
    private void setButtonsContentDisplay() {
        if (sidebarRoot == null) return;

        for (Node n : sidebarRoot.getChildren()) {
            // Only affect styled sidebar buttons
            if (n instanceof Button && n.getStyleClass().contains("menu-button")) {
                ((Button) n).setContentDisplay(ContentDisplay.LEFT);
            }
        }
    }

    /**
     * Applies Role-Based Access Control (RBAC).
     * - If the user is NOT a manager, hide pages they should not access.
     * - Managers see everything.
     * 
     * NOTE: Food Menu is visible to everyone by default
     * If you want to restrict it, add: hideButton(foodMenuBtn);
     */
    private void applyRoleRestrictions() {
        String role = SessionManager.getRole();
        System.out.println("[MenuController] Applying RBAC for role: " + role);

        // Only managers should see Reports, Employees
        if (!SessionManager.isManager()) {
            hideButton(reportsBtn);
            hideButton(employeesBtn);
            
            // Uncomment the line below if you want to hide Food Menu from employees:
            // hideButton(foodMenuBtn);
        }
    }

    /**
     * Utility method to hide a button completely.
     * setVisible(false)  -> removes it from display
     * setManaged(false)  -> prevents layout space from being reserved for it
     */
    private void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }

    // ---------------------------------------------------------
    // MENU BUTTON CLICK EVENTS
    // Each method loads a page into the MainController's center pane
    // ---------------------------------------------------------
    
    @FXML 
    private void onDashboard() { 
        loadOrShow("dashboard"); 
    }
    
    @FXML 
    private void onInventory() { 
        loadOrShow("inventory"); 
    }
    
    @FXML 
    private void onAnalytics() {
        System.out.println("[MenuController] Analytics button clicked");
        loadOrShow("analytics");
    }
    
    /**
     * NEW: Food Menu navigation
     * Loads the foodmenu.fxml page when Food Menu button is clicked
     */
    @FXML 
    private void onFoodMenu() {
        System.out.println("[MenuController] Food Menu button clicked");
        loadOrShow("foodmenu");
    }
    
    @FXML 
    private void onReports() { 
        loadOrShow("reports"); 
    }
    
    @FXML 
    private void onFeedback() { 
        loadOrShow("feedback"); 
    }
    
    @FXML 
    private void onEmployees() { 
        loadOrShow("employees"); 
    }
    
    @FXML 
    private void onSettings() {
        System.out.println("[MenuController] Settings button clicked");
        loadOrShow("settings");
    }

    /**
     * A smart page loader:
     * 1. If MainController already exists, load the page into it.
     * 2. If not (e.g., after login), load main.fxml first, then load the page.
     */
    private void loadOrShow(String page) {
        try {
            MainController mc = MainController.getInstance();
            if (mc != null) {
                mc.loadPage(page);
                return; // Done
            }
        } catch (Throwable ignored) {}

        // If no MainController yet (fresh login), load main.fxml first
        try {
            App.setRoot("main");
            MainController mc2 = MainController.getInstance();
            if (mc2 != null) mc2.loadPage(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}