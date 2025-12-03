package main;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

/**
 * Controller for main.fxml.
 * This class keeps a static instance so other controllers (like MenuController)
 * can call loadPage() and change what is displayed in the main content area.
 */
public class MainController {

    // Static reference to this controller so other classes can access it
    private static MainController instance;

    // The container where pages (FXML files) will be loaded
    @FXML
    private StackPane contentPane;

    @FXML
    private void initialize() {
        // Save this controller instance for global access
        instance = this;

        // Load a default page AFTER the UI is fully loaded
        Platform.runLater(() -> loadPage("dashboard"));
    }

    /**
     * Returns the shared MainController instance.
     * This will be used by other controllers to load pages.
     */
    public static MainController getInstance() {
        return instance;
    }

    /**
     * Loads an FXML page into the content area.
     * Example: loadPage("dashboard") will load /main/dashboard.fxml
     *
     * If the file does not exist, it will show an error message instead of crashing.
     */
    public void loadPage(String page) {
        // If contentPane is not ready, stop here
        if (contentPane == null) return;

        try {
            System.out.println("[MainController] Loading page: " + page);
            // Build the file path to the FXML
            String resourcePath = "/main/" + page + ".fxml";

            // Try to locate the file
            URL url = App.class.getResource(resourcePath);
            System.out.println("[MainController] Resource path: " + resourcePath + " -> " + url);
            if (url == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            // Load the FXML file and place it inside the content pane
            Node node = FXMLLoader.load(url);
            contentPane.getChildren().setAll(node);

        } catch (IOException e) {
            // If something goes wrong, show an error message on the screen
            e.printStackTrace();
            contentPane.getChildren().clear();

            Label lbl = new Label("Page not found: " + page);
            lbl.setStyle("-fx-font-size:16; -fx-text-fill:#333;");
            contentPane.getChildren().add(lbl);
        }
    }
}
