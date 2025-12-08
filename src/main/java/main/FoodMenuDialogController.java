package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class FoodMenuDialogController 
{
    @FXML
    private ImageView imageUpload;

    @FXML
    private TextField mealLabel;

    @FXML
    private ChoiceBox<String> categoryBox;

    @FXML
    private ChoiceBox<String> typeBox;

    @FXML
    private TextField priceField;

    @FXML
    private TextArea descriptionText;

    @FXML
    private Button okayButton;

    @FXML
    private Button cancelButton;

    private File selectedImageFile;
    private String savedImageName;

    @FXML
    public void initialize()
    {
        imageUpload.setOnMouseClicked(event -> handleImageUpload());
        loadCategories();
        loadTypes();
    }

    // Loads meal categories and stores it in a ObservableList
    @FXML
    private void loadCategories()
    {
        ObservableList<String> categoryList = FXCollections.observableArrayList();

        String dbUrl = "jdbc:sqlite:database/lamesa.db";
        System.out.println("[FoodMenuDialogController] Connecting to: " + dbUrl);
        try(Connection conn = DriverManager.getConnection(dbUrl))
        {
            System.out.println("[FoodMenuDialogController] Connected successfully!");
            String sql = "SELECT category_name FROM meal_category ORDER BY category_id";
            try(PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery())
                {
                    while(rs.next())
                    {
                        String category = rs.getString("category_name");
                        categoryList.add(category);
                    }

                    categoryBox.setItems(categoryList);
                }
        }
        catch (SQLException e) 
        {
            System.out.println("[FoodMenuDialogController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Loads meal types and stores it in a ObservableList
    
    private void loadTypes()
    {
        ObservableList<String> typeList = FXCollections.observableArrayList();

        String dbUrl = "jdbc:sqlite:database/lamesa.db";
        System.out.println("[FoodMenuDialogController] Connecting to: " + dbUrl);
        try(Connection conn = DriverManager.getConnection(dbUrl))
        {
            System.out.println("[FoodMenuDialogController] Connected successfully!");
            String sql = "SELECT type_name FROM meal_types ORDER BY type_id";
            try(PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery())
                {
                    while(rs.next())
                    {
                        String type = rs.getString("type_name");
                        typeList.add(type);
                    }

                    typeBox.setItems(typeList);
                }
        }
        catch (SQLException e) 
        {
            System.out.println("[FoodMenuDialogController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handles the uploading of Image
    
    private void handleImageUpload()
    {
        // Makes user upload an Image
        FileChooser fc = new FileChooser();
        fc.setTitle("Upload an Image");
        fc.getExtensionFilters().addAll
        (new ExtensionFilter("Image Files", "*.png", "*.jpg","*.jpeg"));

        // Shows the uploaded Image directly if user uploaded
        File file = fc.showOpenDialog(imageUpload.getScene().getWindow());
        if(file != null)
        {
            selectedImageFile = file;
            imageUpload.setImage(new Image(file.toURI().toString()));
        }
    }

    // This saves the image uploaded to Assets Folder
    
    private String saveImageToAssets()
    {   
        try 
        {
            if(selectedImageFile == null) 
            return null;
        
        Path assetsFolder = Path.of("src/main/resources/assets");
        String ext = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf(".")); // Gets the file extension ".png, .jpg, .jpeg"
        String newName = "food_menu_item_#" + System.currentTimeMillis() + ext;
        Files.copy(
            selectedImageFile.toPath(),             // Source file (where to copy FROM)
            assetsFolder.resolve(newName),          // Destination (where to copy TO)
            StandardCopyOption.REPLACE_EXISTING);   // If file exists, overwrite it

            return newName;
        }
        catch (IOException e)
        {
            System.out.println("[FoodMenuDialogController] ERROR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private int getCategoryId(String categoryName)
    {
        String dbUrl = "jdbc:sqlite:database/lamesa.db";
        System.out.println("[FoodMenuDialogController] Connecting to: " + dbUrl);
        try(Connection conn = DriverManager.getConnection(dbUrl))
        {
            System.out.println("[FoodMenuDialogController] Connected successfully!");
            String sql = "SELECT category_id FROM meal_category WHERE category_name = ?";
            try(PreparedStatement ps = conn.prepareStatement(sql))
                {
                  ps.setString(1, categoryName);

                  try(ResultSet rs = ps.executeQuery())
                  {
                    if(rs.next())
                        return rs.getInt("category_id");
                  }
                }
        }
        catch (SQLException e) 
        {
            System.out.println("[FoodMenuDialogController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; //Return a -1 if not found
    }
}