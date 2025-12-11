package main;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class DashboardController implements Initializable {

    @FXML
    private Label usernameLabel;

    @FXML
    private Text totalOrdersYearText;

    @FXML
    private Text activeEmployeesText;

    @FXML
    private Text mealsSoldText;

    @FXML
    private Text totalRevenueText;

    @FXML
    private VBox lowStockVBox;

    @FXML
    private VBox staffPerformanceVBox;

    public static class Database {
        private static final String DB_URL = "jdbc:sqlite:database/lamesa.db";

        public static Connection getConnection() throws Exception {
            Class.forName("org.sqlite.JDBC"); // Load JDBC driver
            return DriverManager.getConnection(DB_URL);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load dashboard data on startup
        loadDashboardData();
    }

    // Set welcome message
    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    // Load dashboard data
    private void loadDashboardData() {
        try {
            Connection conn = Database.getConnection();

            // Get total orders for current year
            String orderYearSql = "SELECT COUNT(*) as orderCount FROM sales WHERE strftime('%Y', sale_date) = strftime('%Y', 'now')";
            PreparedStatement orderYearStmt = conn.prepareStatement(orderYearSql);
            ResultSet orderYearRs = orderYearStmt.executeQuery();
            
            if (orderYearRs.next()) {
                totalOrdersYearText.setText(String.valueOf(orderYearRs.getInt("orderCount")));
            }

            // Get total revenue for current month in pesos (no decimals)
            String revenueSql = "SELECT SUM(total_price) AS totalRevenue FROM sales WHERE strftime('%Y-%m', sale_date) = strftime('%Y-%m', 'now')";
            PreparedStatement revenueStmt = conn.prepareStatement(revenueSql);
            ResultSet revenueRs = revenueStmt.executeQuery();
            
            if (revenueRs.next()) {
                double revenue = revenueRs.getDouble("totalRevenue");
                totalRevenueText.setText(String.format("₱%.0f", revenue));
            }

            // Get total employee count (all employees including managers)
            String employeeSql = "SELECT COUNT(*) as totalCount FROM users";
            PreparedStatement employeeStmt = conn.prepareStatement(employeeSql);
            ResultSet employeeRs = employeeStmt.executeQuery();
            
            if (employeeRs.next()) {
                activeEmployeesText.setText(String.valueOf(employeeRs.getInt("totalCount")));
            }

            // Get total meals sold this month
            String mealsSql = "SELECT SUM(quantity) as totalMeals FROM sales WHERE strftime('%Y-%m', sale_date) = strftime('%Y-%m', 'now')";
            PreparedStatement mealsStmt = conn.prepareStatement(mealsSql);
            ResultSet mealsRs = mealsStmt.executeQuery();
            
            if (mealsRs.next()) {
                int meals = mealsRs.getInt("totalMeals");
                mealsSoldText.setText(String.valueOf(meals));
            }

            // Load Low Stock Items (less than 15 units) with meal names
            String lowStockSql = "SELECT m.name, i.stock_quantity FROM inventory i JOIN meal m ON i.meal_id = m.meal_id WHERE i.stock_quantity < 15 ORDER BY i.stock_quantity ASC LIMIT 5";
            PreparedStatement lowStockStmt = conn.prepareStatement(lowStockSql);
            ResultSet lowStockRs = lowStockStmt.executeQuery();
            
            lowStockVBox.getChildren().clear();
            boolean hasLowStock = false;
            while (lowStockRs.next()) {
                hasLowStock = true;
                String mealName = lowStockRs.getString("name");
                int quantity = lowStockRs.getInt("stock_quantity");
                
                HBox itemRow = new HBox(15);
                itemRow.setPadding(new Insets(12, 15, 12, 15));
                itemRow.setStyle("-fx-border-color: #efefef; -fx-border-width: 0 0 1 0; -fx-alignment: CENTER_LEFT;");
                
                Label nameLabel = new Label(mealName);
                nameLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #333; -fx-font-family: 'System';");
                nameLabel.setMaxWidth(450);
                nameLabel.setWrapText(true);
                
                Label quantityLabel = new Label(quantity + " units");
                quantityLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-family: 'System';");
                quantityLabel.setPrefWidth(120);
                quantityLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                
                itemRow.getChildren().addAll(nameLabel, quantityLabel);
                lowStockVBox.getChildren().add(itemRow);
            }
            
            if (!hasLowStock) {
                Label noDataLabel = new Label("All items well stocked ✓");
                noDataLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #27ae60; -fx-padding: 20; -fx-font-family: 'System';");
                noDataLabel.setAlignment(javafx.geometry.Pos.CENTER);
                lowStockVBox.getChildren().add(noDataLabel);
            }

            // Load Staff Performance (based on login frequency)
            String staffSql = "SELECT username, COUNT(*) as login_count FROM attendance GROUP BY username ORDER BY login_count DESC LIMIT 5";
            PreparedStatement staffStmt = conn.prepareStatement(staffSql);
            ResultSet staffRs = staffStmt.executeQuery();
            
            staffPerformanceVBox.getChildren().clear();
            int rank = 1;
            boolean hasStaff = false;
            while (staffRs.next()) {
                hasStaff = true;
                String username = staffRs.getString("username");
                int loginCount = staffRs.getInt("login_count");
                
                HBox staffRow = new HBox(15);
                staffRow.setPadding(new Insets(12, 15, 12, 15));
                staffRow.setStyle("-fx-border-color: #efefef; -fx-border-width: 0 0 1 0; -fx-alignment: CENTER_LEFT;");
                
                Label rankLabel = new Label("#" + rank);
                rankLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #999; -fx-font-weight: bold; -fx-font-family: 'System';");
                rankLabel.setPrefWidth(35);
                rankLabel.setAlignment(javafx.geometry.Pos.CENTER);
                
                Label nameLabel = new Label(username);
                nameLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #333; -fx-font-family: 'System';");
                nameLabel.setPrefWidth(350);
                
                Label logsLabel = new Label(loginCount + " logins");
                logsLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-family: 'System';");
                logsLabel.setPrefWidth(120);
                logsLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                
                staffRow.getChildren().addAll(rankLabel, nameLabel, logsLabel);
                staffPerformanceVBox.getChildren().add(staffRow);
                rank++;
            }
            
            if (!hasStaff) {
                Label noDataLabel = new Label("No login data yet");
                noDataLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #999; -fx-padding: 20; -fx-font-family: 'System';");
                noDataLabel.setAlignment(javafx.geometry.Pos.CENTER);
                staffPerformanceVBox.getChildren().add(noDataLabel);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

