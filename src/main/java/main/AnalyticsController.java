package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.DatePicker; 
import javafx.scene.control.Button; 
import java.time.LocalDate; 


import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;





public class AnalyticsController {

    @FXML
    private LineChart<String, Number> lineChart; 

    @FXML 
    private PieChart pieChart;

    @FXML
    private Label TotalMonthlySalesLabel;

    @FXML 
    private Label TotalYearlySalesLabel; 


    @FXML 
    private Label MonthlySalesTitle; 


    @FXML 
    private Label YearlySalesTitle;

    @FXML
    private javafx.scene.layout.HBox customLegend;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private DatePicker StartDatePicker;

    @FXML
    private DatePicker EndDatePicker;

       String DB_URL = "jdbc:sqlite:database/lamesa.db";




        private String loadTotalMonthlySales() throws SQLException { 

            
       String CurrentmonthSales = "SELECT SUM(total_price) AS total_Sales_Current_Month" + 
                             " FROM sales " + "WHERE strftime('%Y-%m', sale_date) = strftime('%Y-%m', 'now');";

        try(Connection conn = DriverManager.getConnection(DB_URL); 
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(CurrentmonthSales)) {

                while(rs.next()) { 
                    double totalmonthlysales =  rs.getDouble("total_Sales_Current_Month");
                    return String.format("%.0f", totalmonthlysales);
                }

                                
            } catch(SQLException e){
                System.err.println("Error loading total monthly sales: " + e.getMessage()); 
            }
            return "0.00";
        }


        private String loadTotalYearlySales() throws SQLException { 


               String CurrentYearSales = "SELECT SUM(total_price) AS total_Sales_Current_Year" + 
                             " FROM sales " + "WHERE strftime('%Y', sale_date) = strftime('%Y', 'now');";

            try(Connection conn = DriverManager.getConnection(DB_URL); 
                Statement stmt = conn.createStatement(); 
                ResultSet rs = stmt.executeQuery(CurrentYearSales)) { 

                    while(rs.next()) { 
                        double totalyearlysales = rs.getDouble("total_Sales_Current_Year"); 
                        return String.format("%.0f", totalyearlysales);
                    }

                }catch(SQLException e) { 
                    System.err.println("Error loading total yearly sales: " + e.getMessage()); 
                }

                return "0.00";

        }





        private void loadTopMealsData() throws SQLException { 
     
   

        String topMealsQuery = "SELECT name, SUM(quantity) AS total_sold" + 
                                " FROM sales " +
                                " JOIN meal using(meal_id) " +
                                " GROUP BY meal_id, name " +
                                " ORDER BY total_sold DESC " +
                                " LIMIT 5;";


        try(Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement(); 
            ResultSet rs = stmt.executeQuery(topMealsQuery)) { 


                while(rs.next()) { 
                    String mealName = rs.getString("name"); 
                    int totalSold = rs.getInt("total_sold"); 
                    pieChart.getData().add(new PieChart.Data(mealName, totalSold)); 
                }
            } catch (SQLException e) { 
                System.err.println("Error"); 
                e.printStackTrace();
            }

        }



        private void loadMonthlySalesData() throws SQLException { 
            String monthlySalesQuery = "SELECT strftime('%Y-%m', sale_date) AS month, SUM(total_price) AS total_sales " +
                                        "FROM sales " +
                                        "GROUP BY strftime('%Y-%m', sale_date) " + 
                                        "ORDER BY month ASC"; 

        
            try(Connection conn = DriverManager.getConnection(DB_URL); 
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(monthlySalesQuery)) { 

                XYChart.Series <String, Number> series = new XYChart.Series<>(); 
                series.setName("Monthly Sales"); 
                

                while(rs.next()) { 
                    String month = rs.getString("month"); 
                    double totalsales = rs.getDouble("total_sales");
                    
                    // Convert "2024-12" to "December"
                    java.time.YearMonth ym = java.time.YearMonth.parse(month);
                    String monthName = ym.format(java.time.format.DateTimeFormatter.ofPattern("MMMM"));
                    
                    series.getData().add(new XYChart.Data<>(monthName, totalsales));
                }
                

                lineChart.getData().add(series); 
            } catch (SQLException e) { 
                System.err.println("Error loading monthly sales data: " + e.getMessage()); 
                e.printStackTrace();
            }

        }



        private String displayCurrentMonth() 
        {
            LocalDate today = LocalDate.now(); 
            String currentMonth = today.format(DateTimeFormatter.ofPattern("MMMM")); 
            return currentMonth; 
        }

        private String displayCurrentYear()
        { 
            LocalDate today = LocalDate.now(); 
            String currentYear = today.format(DateTimeFormatter.ofPattern("yyyy")); 
            return currentYear;
        }



        private void SalesByProduct() throws SQLException
        { 
            String lowestToHighest = "SELECT name, SUM(quantity) AS total_sold " + 
                                "FROM sales " +
                                "JOIN meal USING(meal_id) " + 
                                "GROUP BY meal_id, name " + 
                                "ORDER BY total_sold ASC;"; 

            try(Connection conn = DriverManager.getConnection(DB_URL); 
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(lowestToHighest)) { 

                    XYChart.Series<String, Number> series = new XYChart.Series<>(); 
                    series.setName("Quantity Sold"); 

                    while(rs.next()) { 
                        series.getData().add(new XYChart.Data<>(rs.getString("name"), rs.getInt("total_sold"))); 
                    }
                    
                    barChart.getData().add(series);
                } catch (SQLException e){
                    System.err.println("Error loading total sales per product: " + e.getMessage()); 
                    e.printStackTrace();
                }
        }




    private void loadMonthlySalesDataWithDateRange(LocalDate startDate, LocalDate endDate) throws SQLException { 
        String monthlySalesQuery = "SELECT strftime('%Y-%m', sale_date) AS month, SUM(total_price) AS total_sales " +
                                    "FROM sales " +
                                    "WHERE sale_date BETWEEN ? AND ? " +
                                    "GROUP BY strftime('%Y-%m', sale_date) " + 
                                    "ORDER BY month ASC"; 

        try(Connection conn = DriverManager.getConnection(DB_URL); 
            PreparedStatement pstmt = conn.prepareStatement(monthlySalesQuery)) {
            
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            
            try(ResultSet rs = pstmt.executeQuery()) {
                XYChart.Series<String, Number> series = new XYChart.Series<>(); 
                series.setName("Monthly Sales"); 
                
                while(rs.next()) { 
                    String month = rs.getString("month"); 
                    double totalsales = rs.getDouble("total_sales");
                    
                    java.time.YearMonth ym = java.time.YearMonth.parse(month);
                    String monthName = ym.format(java.time.format.DateTimeFormatter.ofPattern("MMMM"));
                    
                    series.getData().add(new XYChart.Data<>(monthName, totalsales));
                }
                
                lineChart.getData().add(series);
                
                if (lineChart.getData().size() > 0) {
                    XYChart.Series<String, Number> s = lineChart.getData().get(0);
                    s.getNode().setStyle("-fx-stroke: #3ab68dff; -fx-stroke-width: 3;");
                    for (XYChart.Data<String, Number> data : s.getData()) {
                        if (data.getNode() != null) {
                            data.getNode().setStyle("-fx-background-color: #3ab68dff;");
                        }
                    }
                }
            }
        } catch (SQLException e) { 
            System.err.println("Error loading monthly sales data: " + e.getMessage()); 
            e.printStackTrace();
        }
    }

    private void loadTopMealsDataWithDateRange(LocalDate startDate, LocalDate endDate) throws SQLException { 
        String topMealsQuery = "SELECT name, SUM(quantity) AS total_sold " +
                                "FROM sales " +
                                "JOIN meal USING(meal_id) " +
                                "WHERE sale_date BETWEEN ? AND ? " +
                                "GROUP BY meal_id, name " +
                                "ORDER BY total_sold DESC " +
                                "LIMIT 5";

        try(Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(topMealsQuery)) {
            
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            
            try(ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) { 
                    String mealName = rs.getString("name"); 
                    int totalSold = rs.getInt("total_sold"); 
                    pieChart.getData().add(new PieChart.Data(mealName, totalSold)); 
                }
            }
        } catch (SQLException e) { 
            System.err.println("Error loading top meals data: " + e.getMessage()); 
            e.printStackTrace();
        }
    }

    private String loadTotalMonthlySalesWithDateRange(LocalDate startDate, LocalDate endDate) throws SQLException { 
        String query = "SELECT SUM(total_price) AS total_sales FROM sales WHERE sale_date BETWEEN ? AND ? AND strftime('%Y-%m', sale_date) = strftime('%Y-%m', 'now')";

        try(Connection conn = DriverManager.getConnection(DB_URL); 
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            
            try(ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()) { 
                    double total = rs.getDouble("total_sales");
                    return String.format("%.0f", total);
                }
            }
        } catch(SQLException e){
            System.err.println("Error loading total monthly sales: " + e.getMessage()); 
        }
        return "0";
    }

    private String loadTotalYearlySalesWithDateRange(LocalDate startDate, LocalDate endDate) throws SQLException { 
        String query = "SELECT SUM(total_price) AS total_sales FROM sales WHERE sale_date BETWEEN ? AND ? AND strftime('%Y', sale_date) = strftime('%Y', 'now')";

        try(Connection conn = DriverManager.getConnection(DB_URL); 
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            
            try(ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()) { 
                    double total = rs.getDouble("total_sales");
                    return String.format("%.0f", total);
                }
            }
        } catch(SQLException e) { 
            System.err.println("Error loading total yearly sales: " + e.getMessage()); 
        }
        return "0";
    }

    @FXML
    public void HandleFilterDate() {
        LocalDate startDate = StartDatePicker.getValue();
        LocalDate endDate = EndDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            System.err.println("Please select both start and end dates");
            return;
        }
        
        try {
            lineChart.getData().clear();
            pieChart.getData().clear();
            customLegend.getChildren().clear();
            
            loadMonthlySalesDataWithDateRange(startDate, endDate);
            loadTopMealsDataWithDateRange(startDate, endDate);
            
            TotalMonthlySalesLabel.setText(loadTotalMonthlySalesWithDateRange(startDate, endDate) + " ₱");
            TotalYearlySalesLabel.setText(loadTotalYearlySalesWithDateRange(startDate, endDate) + " ₱");
            
            final String[] colors = {"#114F3A", "#1A6B4F", "#228866", "#2BA47C", "#34C191"};
            int index = 0;
            for (PieChart.Data data : pieChart.getData()) {
                String color = colors[index % colors.length];
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
                
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(4);
                circle.setFill(javafx.scene.paint.Color.web(color));
                javafx.scene.control.Label label = new javafx.scene.control.Label(data.getName());
                label.setStyle("-fx-font-size: 11;");
                javafx.scene.layout.HBox legendItem = new javafx.scene.layout.HBox(4);
                legendItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                legendItem.getChildren().addAll(circle, label);
                customLegend.getChildren().add(legendItem);
                
                index++;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating charts: " + e.getMessage());
            e.printStackTrace();
        }
    }

       
    @FXML
    public void initialize() {
        // Set default date range (last 12 months)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(12);
        
        StartDatePicker.setValue(startDate);
        EndDatePicker.setValue(endDate);
        
        try {
            loadMonthlySalesData();
            // Color the line chart line
            if (lineChart.getData().size() > 0) {
                XYChart.Series<String, Number> series = lineChart.getData().get(0);
                // Style the series line and legend
                series.getNode().setStyle("-fx-stroke: #3ab68dff; -fx-stroke-width: 3;");
                // Color the data points
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-background-color: #3ab68dff;");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error initializing monthly sales data: " + e.getMessage());
            e.printStackTrace();
        }
     try {
    loadTopMealsData();

    final String[] colors = {"#114F3A", "#1A6B4F", "#228866", "#2BA47C", "#34C191"};
    int index = 0;

    // Clear custom legend
    customLegend.getChildren().clear();

    for (PieChart.Data data : pieChart.getData()) {
        String color = colors[index % colors.length];
        data.getNode().setStyle("-fx-pie-color: " + color + ";");
        
        // Add to custom legend
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(4);
        circle.setFill(javafx.scene.paint.Color.web(color));
        javafx.scene.control.Label label = new javafx.scene.control.Label(data.getName());
        label.setStyle("-fx-font-size: 11;");
        javafx.scene.layout.HBox legendItem = new javafx.scene.layout.HBox(4);
        legendItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        legendItem.getChildren().addAll(circle, label);
        customLegend.getChildren().add(legendItem);
        
        index++;
    }

} catch (SQLException e) {
    e.printStackTrace();
}

        try {
            TotalMonthlySalesLabel.setText(loadTotalMonthlySales() + " ₱");
        } catch (SQLException e) { 
            System.err.println("Error settint total monthly sales label" + e.getMessage()); 
        }


        try{ 
            TotalYearlySalesLabel.setText(loadTotalYearlySales() + " ₱");
        } catch (SQLException e) { 
            System.err.println("Error setting total yearly sales label: " + e.getMessage());
        }

        try { 
            MonthlySalesTitle.setText(displayCurrentMonth()); 
            YearlySalesTitle.setText(displayCurrentYear());
        } catch(Exception e) { 
            System.err.println("Error in setting sales titles: " + e.getMessage());
        }

        try{
            SalesByProduct();
        } catch(Exception e) {
            System.err.println("Error loading Sales by product" + e.getMessage());
        }
    }

}
