package main;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.net.URL;

/**
 * Entry point for the JavaFX application.
 *
 * Responsibilities:
 * - Seed a development test user on startup (DatabaseSeeder.seedTestUser()).
 * - Load the initial FXML (login screen) and manage scene root swaps.
 *
 * Notes for the team:
 * - setRoot(...) returns a boolean indicating whether navigation succeeded.
 * - loadFXML(...) throws a clear IOException when an FXML resource is missing.
 * - For production, remove the DatabaseSeeder call and replace SHA-256 password hashing
 *   with a stronger algorithm (bcrypt/argon2).
 */
public class App extends javafx.application.Application {

    // Single Scene instance reused for the entire app.
    // Controllers will ask App.setRoot(...) to swap the scene's root node.
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // ---- Development helper (remove for production) ----
        // Ensure there is a known test user available for quick testing and debugging.
        DatabaseSeeder.seedTestUser();

        // ---- Initial UI setup ----
        // Load the login screen FXML and create the primary Scene with a starting size.
        scene = new Scene(loadFXML("login"), 1200, 700);

        // Set window title and show the stage.
        stage.setTitle("Lamesa");
        stage.setScene(scene);
        stage.show();

        // Small console trace to help debug navigation/startup problems.
        System.out.println("[App] Started. Showing: login");
    }

    /**
     * Swap the scene root to the FXML identified by the simple name (e.g. "main", "login").
     *
     * Behaviour:
     * - Loads the requested FXML via loadFXML(...)
     * - Schedules root change on the JavaFX Application thread using Platform.runLater(...)
     * - Returns true when navigation was initiated, false when the load failed.
     *
     * If loading fails, shows an error Alert with the exception stack trace to help
     * developers find broken/missing FXML quickly.
     *
     * @param fxml simple FXML name (without .fxml)
     * @return true if navigation was initiated successfully, false otherwise
     */
    static boolean setRoot(String fxml) {
        try {
            // Attempt to load the requested FXML file -> returns a Parent node (root).
            Parent root = loadFXML(fxml);

            // Updating scene root must happen on the JavaFX Application Thread.
            // runLater ensures the swap happens safely if called from another thread.
            Platform.runLater(() -> {
                scene.setRoot(root);
                System.out.println("[App] Navigated to: " + fxml);
            });

            return true;
        } catch (IOException e) {
            // Console logging for quick debugging in the terminal/IDE.
            System.err.println("[App] Failed to navigate to: " + fxml + " — " + e.getMessage());
            e.printStackTrace();

            // Show a developer-friendly dialog with the exception details.
            // Keep the Alert on the FX thread as well.
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Navigation Error");
                alert.setHeaderText("Unable to open: " + fxml);

                // Convert the exception stack trace into a string to display inside the alert.
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionText = sw.toString();

                // Short message visible in the alert body.
                alert.setContentText(e.getMessage());

                // Place full stack trace inside a non-editable TextArea so developers can expand it.
                javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(exceptionText);
                textArea.setEditable(false);
                textArea.setWrapText(false);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);

                // Use a VBox to hold the expandable content.
                javafx.scene.layout.VBox dialogPaneContent = new javafx.scene.layout.VBox();
                dialogPaneContent.getChildren().add(textArea);
                alert.getDialogPane().setExpandableContent(dialogPaneContent);

                alert.showAndWait();
            });

            return false;
        }
    }

    /**
     * Load an FXML resource from src/main/resources/main/<fxml>.fxml.
     *
     * This method performs a defensive check and throws IOException with a clear message if
     * the requested resource is not found. Controllers call setRoot(...) which uses this loader.
     *
     * @param fxml simple FXML name (without .fxml)
     * @return the loaded Parent root node
     * @throws IOException when the resource cannot be found or fails to load
     */
    private static Parent loadFXML(String fxml) throws IOException {
        // Construct the resource path using the convention used by the project.
        String resourcePath = "/main/" + fxml + ".fxml";

        // Use the App class loader to find the file inside resources.
        URL resource = App.class.getResource(resourcePath);

        // If the resource isn't found, throw an explicit IOException with helpful text.
        // This makes it obvious when an FXML file is misnamed or missing.
        if (resource == null) {
            throw new IOException("FXML resource not found: " + resourcePath);
        }

        // Create the FXMLLoader with the located resource and load the UI graph.
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        return fxmlLoader.load();
    }

    /**
     * Standard JavaFX launcher. Keeps main(...) minimal.
     */
    public static void main(String[] args) {
        launch();
    }
}
