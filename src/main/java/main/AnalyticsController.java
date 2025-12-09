package main;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart; 
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label; 
import java.time.LocalDate;
import java.time.YearMonth; 
import java.time.format.DateTimeFormatter; 


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;





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
                    series.getData().add(new XYChart.Data<>(month, totalsales));
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



       

    @FXML
    public void initialize() {
        try {
            loadMonthlySalesData();
        } catch (SQLException e) {
            System.err.println("Error initializing monthly sales data: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            loadTopMealsData();
        } catch (SQLException e) {
            System.err.println("Error initializing top meals data: " + e.getMessage());
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




    }

}
