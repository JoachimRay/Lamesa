package main;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart; 
import javafx.scene.chart.XYChart; 

public class AnalyticsController {

    @FXML
    private LineChart<String, Number> lineChart; 

    @FXML 
    private PieChart pieChart;

    

    @FXML
    public void initialize() {
       XYChart.Series<String, Number> series = new XYChart.Series<>(); 
        series.setName("2025 Sales"); 
        series.getData().add(new XYChart.Data<>("Jan", 120));
        series.getData().add(new XYChart.Data<>("Feb", 150));
        series.getData().add(new XYChart.Data<>("Mar", 180));
        series.getData().add(new XYChart.Data<>("Apr", 140));

        lineChart.getData().add(series);

        pieChart.getData().add(new PieChart.Data("Electronics", 40));
        pieChart.getData().add(new PieChart.Data("Clothing", 30));
        pieChart.getData().add(new PieChart.Data("Groceries", 20));


    }

    


}
