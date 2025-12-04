package main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * EditEmployeeController
 *
 * - Dialog controller for edit_employee.fxml
 * - Pre-fills current username and role
 * - Saves role via EmployeeDAO.updateRoleByUsername(...)
 */
public class EditEmployeeController {

    @FXML private Label usernameLabel;
    @FXML private ComboBox<String> roleCombo;

    private String username; // set by caller

    @FXML
    private void initialize() {
        // Populate role options (add more if you support more roles)
        roleCombo.getItems().addAll("employee", "manager");
    }

    /**
     * Called by EmployeesController before showing the dialog.
     * Pre-fills the UI with values.
     */
    public void setInitialValues(String username, String currentRole) {
        this.username = username;
        usernameLabel.setText(username);
        if (currentRole != null) {
            roleCombo.setValue(currentRole.toLowerCase());
        } else {
            roleCombo.setValue("employee");
        }
    }

    /**
     * Save the new role into the database and close the dialog.
     */
    @FXML
    private void onSave() {
        String selected = roleCombo.getValue();
        if (selected == null || selected.isEmpty()) return;

        EmployeeDAO dao = new EmployeeDAO();
        boolean ok = dao.updateRoleByUsername(username, selected);
        if (!ok) {
            // show minimal feedback (you can replace with Alert)
            System.err.println("[EditEmployeeController] Failed to update role for " + username);
        }
        closeWindow();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage s = (Stage) usernameLabel.getScene().getWindow();
        s.close();
    }
}
