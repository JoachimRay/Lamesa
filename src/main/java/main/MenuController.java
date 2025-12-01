package main;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

/**
 * Controller for the sidebar menu.
 * Responsibilities:
 * - Expanding and collapsing the sidebar with animation
 * - Changing button display (icon only vs icon + text)
 * - Loading pages by calling MainController when menu items are clicked
 */
public class MenuController {

    @FXML
    private VBox sidebarRoot; // The whole sidebar area

    @FXML
    private Button toggleButton; // Button used to collapse/expand the sidebar

    // Sidebar widths for expanded and collapsed modes
    private final double expandedWidth = 220;
    private final double collapsedWidth = 64;

    // Animation speed
    private final Duration animationDuration = Duration.millis(180);

    @FXML
    private Button dashboardButton; // add fx:id in FXML
    @FXML
    private Button inventoryButton;
    @FXML
    private Button ordersButton;
    @FXML
    private Button feedbacksButton;
    @FXML
    private Button employeesButton;
    @FXML
    private Button settingsButton;
    


    @FXML
    private void initialize() {
        // Set the default sidebar width (expanded)
        if (sidebarRoot != null) {
            sidebarRoot.setPrefWidth(expandedWidth);

            // Show icons + text (normal expanded view)
            setButtonsContentDisplay(false);
        }

        // ================= RBAC: Role-based access control =================
        // Hide manager-only menu items if current user is employee
        if (SessionManager.isEmployee()) {
            if (employeesButton != null)
                employeesButton.setVisible(false);
            if (settingsButton != null)
                settingsButton.setVisible(false);
        }
        // ==================================================================
    }

    /**
     * Called when the toggle button (hamburger menu) is clicked.
     * Expands or collapses the sidebar.
     */
    @FXML
    private void onToggleSidebar() {
        if (sidebarRoot == null)
            return;

        // Check current state using a CSS class
        final boolean isCollapsed = sidebarRoot.getStyleClass().contains("collapsed");

        if (isCollapsed) {
            // Expand sidebar
            sidebarRoot.getStyleClass().remove("collapsed");
            animateWidth(collapsedWidth, expandedWidth);
            setButtonsContentDisplay(false); // Show text again
        } else {
            // Collapse sidebar
            sidebarRoot.getStyleClass().add("collapsed");
            animateWidth(expandedWidth, collapsedWidth);
            setButtonsContentDisplay(true); // Hide text, show icons only
        }
    }

    /**
     * Smoothly animates the sidebar’s width change.
     */
    private void animateWidth(double from, double to) {
        if (sidebarRoot == null)
            return;

        Timeline tl = new Timeline();
        KeyValue kv = new KeyValue(sidebarRoot.prefWidthProperty(), to);
        KeyFrame kf = new KeyFrame(animationDuration, kv);

        // Set the starting width
        sidebarRoot.setPrefWidth(from);

        tl.getKeyFrames().add(kf);
        tl.play(); // Play animation
    }

    /**
     * Controls how menu buttons display their content:
     * - graphicOnly = true → show only icons
     * - graphicOnly = false → show icons + text
     *
     * Also hides the brand label when collapsed.
     */
    private void setButtonsContentDisplay(boolean graphicOnly) {
        if (sidebarRoot == null)
            return;

        List<Node> children = sidebarRoot.getChildren();
        for (Node n : children) {

            // Handle regular menu buttons
            if (n instanceof Button && n.getStyleClass().contains("menu-button")) {
                Button b = (Button) n;

                if (graphicOnly) {
                    // Collapse mode: icons only
                    b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                    // Store text in accessibility text so it's not lost
                    if (b.getText() != null && !b.getText().isEmpty()) {
                        b.setAccessibleText(b.getText());
                    }
                } else {
                    // Expanded mode: show icon + text
                    b.setContentDisplay(ContentDisplay.LEFT);
                }
            }

            // Handle the sidebar brand label (app name/logo text)
            else if (n instanceof Label && ((Label) n).getStyleClass().contains("brand")) {
                ((Label) n).setVisible(!graphicOnly);
                ((Label) n).setManaged(!graphicOnly); // Remove spacing when hidden
            }
        }
    }

    // Menu item handlers — these call loadOrShow() to load the correct page
    @FXML
    private void onDashboard() {
        loadOrShow("dashboard");
    }

    @FXML
    private void onInventory() {
        loadOrShow("inventory");
    }

    @FXML
    private void onOrders() {
        loadOrShow("orders");
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
        loadOrShow("settings");
    }

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
     * If MainController is not ready yet, it loads main.fxml first and then
     * retries.
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
