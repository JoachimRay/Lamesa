package main;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller for the sidebar menu.
 * Responsibilities:
 *  - Managing menu button display and layout
 *  - Loading pages by calling MainController when menu items are clicked
 */
public class MenuController {

    @FXML
    private VBox sidebarRoot;   // The whole sidebar area

    @FXML
    private Button toggleButton; // Toggle button (currently unused)

    @FXML
    private void initialize() {
        // Set the default sidebar width (expanded)
        if (sidebarRoot != null) {
            sidebarRoot.setPrefWidth(300);
            setButtonsContentDisplay();
        }
    }

    /**
     * Called when the toggle button (hamburger menu) is clicked.
     * Currently disabled - sidebar stays at full width.
     */
    @FXML
    private void onToggleSidebar() {
        // Collapse functionality removed
    }

    /**
     * Controls how menu buttons display their content.
     */
    private void setButtonsContentDisplay() {
        if (sidebarRoot == null) return;

        List<Node> children = sidebarRoot.getChildren();
        for (Node n : children) {
            // Handle regular menu buttons - always show text and left alignment
            if (n instanceof Button && n.getStyleClass().contains("menu-button")) {
                Button b = (Button) n;
                b.setContentDisplay(ContentDisplay.LEFT);
            }

            // Handle the sidebar brand label
            else if (n instanceof Label && ((Label)n).getStyleClass().contains("brand")) {
                ((Label)n).setVisible(true);
                ((Label)n).setManaged(true);
            }
        }
    }

    // Menu item handlers — these call loadOrShow() to load the correct page
    @FXML private void onDashboard() { loadOrShow("dashboard"); }
    @FXML private void onInventory() { loadOrShow("inventory"); }
    @FXML private void onOrders() { loadOrShow("orders"); }
    @FXML private void onReports() { loadOrShow("reports"); }
    @FXML private void onFeedback() { loadOrShow("feedback"); }
    @FXML private void onEmployees() { loadOrShow("employees"); }
    @FXML private void onSettings() { loadOrShow("settings"); }

    /**
     * Logs out by switching the root to login.fxml
     */
    @FXML
    private void onLogout() {
        try {
            App.setRoot("login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a page using MainController.
     * If MainController is not ready yet, it loads main.fxml first and then retries.
     */
    private void loadOrShow(String page) {
        try {
            // Try using the existing MainController
            MainController mc = MainController.getInstance();
            if (mc != null) {
                mc.loadPage(page);
                return;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // If MainController was not loaded yet → load main shell then try again
        try {
            App.setRoot("main");
            MainController mc2 = MainController.getInstance();
            if (mc2 != null) {
                mc2.loadPage(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
