package main;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label date;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Text totalOrdersText;

    @FXML
    private Text totalDeliveredText;

    @FXML
    private Text totalCanceledText;

    @FXML
    private Text totalRevenueText;

    public static class Database {
        private static final String DB_URL = "jdbc:sqlite:database/lamesa.db";

        public static Connection getConnection() throws Exception {
            Class.forName("org.sqlite.JDBC"); // Load JDBC driver
            return DriverManager.getConnection(DB_URL);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        
            datePicker.setValue(LocalDate.now());
            date.setText(LocalDate.now().toString());
            filterOrdersByDate(LocalDate.now());

        // Listener for date selection
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                date.setText(newVal.toString());
                filterOrdersByDate(newVal);
            }
        });
    }

    // Set welcome message
    public void setUsername(String username) {
        welcomeLabel.setText("Hi, " + username + "! Welcome Back to Lamesa");
    }

    // Filter DB records by selected date
    private void filterOrdersByDate(LocalDate selectedDate) {
        try {
            Connection conn = Database.getConnection();

            String sql = "SELECT * FROM orders WHERE DATE(order_date) = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selectedDate.toString());

            ResultSet rs = stmt.executeQuery();

            int totalOrders = 0;
            int canceled = 0;
            int delivered = 0;
            double totalRevenue = 0;

            while (rs.next()) {
                totalOrders++;

                String status = rs.getString("status");
                if ("Delivered".equals(status)) delivered++;
                if ("Canceled".equals(status)) canceled++;

                totalRevenue += rs.getDouble("amount");
            }

                totalOrdersText.setText(String.valueOf(totalOrders));
                totalDeliveredText.setText(String.valueOf(delivered));
                totalCanceledText.setText(String.valueOf(canceled));
                totalRevenueText.setText(String.format("$%.2f", totalRevenue));



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

