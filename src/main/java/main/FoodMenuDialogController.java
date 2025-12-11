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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class FoodMenuDialogController 
{
    @FXML
    private StackPane imageContainer;

    @FXML
    private Label uploadLabel;

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
    private FoodMenuItem editingMeal = null;  // null = add mode, not null = edit mode
    private String existingImagePath = null;  // Keep track of existing image when editing

    @FXML
    public void initialize()
    {
        imageContainer.setOnMouseClicked(event -> handleImageUpload());
        loadCategories();
        loadTypes();
    }

    // Set the dialog to edit mode with existing meal data
    public void setEditMode(FoodMenuItem meal) {
        this.editingMeal = meal;
        this.existingImagePath = meal.getImagePath();
        
        // Fill in the form with existing data
        mealLabel.setText(meal.getName());
        priceField.setText(String.valueOf(meal.getPrice()));
        descriptionText.setText(meal.getDescription() != null ? meal.getDescription() : "");
        
        // Set category dropdown
        if (meal.getCategoryName() != null) {
            categoryBox.setValue(meal.getCategoryName());
        }
        
        // Set type dropdown
        if (meal.getTypeName() != null) {
            typeBox.setValue(meal.getTypeName());
        }
        
        // Load existing image
        if (meal.getImagePath() != null && !meal.getImagePath().isEmpty()) {
            try {
                String imagePath = meal.getImagePath();
                if (!imagePath.startsWith("assets/")) {
                    imagePath = "assets/" + imagePath;
                }
                Image image = new Image(getClass().getResourceAsStream("/" + imagePath));
                if (image != null && !image.isError()) {
                    imageUpload.setImage(image);
                    uploadLabel.setVisible(false);
                }
            } catch (Exception e) {
                System.out.println("[FoodMenuDialogController] Could not load image: " + meal.getImagePath());
            }
        }
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
        (new ExtensionFilter("Image Files", "*.png", "*.jpg","*.jpeg"));    // Possible types of files that can be uploaded

        // Shows the uploaded Image directly if user uploaded
        File file = fc.showOpenDialog(imageUpload.getScene().getWindow());
        if(file != null)
        {
            selectedImageFile = file;
            imageUpload.setImage(new Image(file.toURI().toString()));
            uploadLabel.setVisible(false);  // Hide the "Click to upload" text
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

            return "assets/" + newName;
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

    private int getTypeId(String typeName)
    {
        String dbUrl = "jdbc:sqlite:database/lamesa.db";
        System.out.println("[FoodMenuDialogController] Connecting to: " + dbUrl);
        try(Connection conn = DriverManager.getConnection(dbUrl))
        {
            System.out.println("[FoodMenuDialogController] Connected successfully!");
            String sql = "SELECT type_id FROM meal_types WHERE type_name = ?";
            try(PreparedStatement ps = conn.prepareStatement(sql))
            {
                ps.setString(1, typeName);
            

                try(ResultSet rs = ps.executeQuery())
                {
                    if(rs.next())
                        return rs.getInt("type_id");
                }
            }
        }
        catch (SQLException e)
        {
            System.out.println("[FoodMenuDialogController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
    
    @FXML
    private void handleOkay()
    {
        String name = mealLabel.getText();
        String category = categoryBox.getValue();
        String type = typeBox.getValue();
        String priceText = priceField.getText();
        String description = descriptionText.getText();

        // Validation
        if (name == null || name.trim().isEmpty()) {
            System.out.println("[FoodMenuDialogController] ERROR: Meal name is required");
            return;
        }
        if (category == null || category.trim().isEmpty()) {
            System.out.println("[FoodMenuDialogController] ERROR: Category is required");
            return;
        }
        if (type == null || type.trim().isEmpty()) {
            System.out.println("[FoodMenuDialogController] ERROR: Type is required");
            return;
        }
        if (priceText == null || priceText.trim().isEmpty()) {
            System.out.println("[FoodMenuDialogController] ERROR: Price is required");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            System.out.println("[FoodMenuDialogController] ERROR: Invalid price format");
            return;
        }

        int category_id = getCategoryId(category);
        int type_id = getTypeId(type);
        
        if (category_id == -1 || type_id == -1) {
            System.out.println("[FoodMenuDialogController] ERROR: Invalid category or type ID");
            return;
        }
        
        // Only save new image if user uploaded one, otherwise keep existing
        String imagePath = saveImageToAssets();
        if (imagePath == null && existingImagePath != null) {
            imagePath = existingImagePath;  // Keep existing image
        }

        String dbUrl = "jdbc:sqlite:database/lamesa.db";
        System.out.println("[FoodMenuDialogController] Connecting to: " + dbUrl);
        try(Connection conn = DriverManager.getConnection(dbUrl))
        {
            System.out.println("[FoodMenuDialogController] Connected successfully!");
            
            String sql;
            if (editingMeal == null) {
                // ADD mode - insert new meal
                sql = "INSERT INTO meal (name, price, category_id, type_id, description, image_path) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
                System.out.println("[FoodMenuDialogController] INSERT mode - Adding new meal");
            } else {
                // EDIT mode - update existing meal
                sql = "UPDATE meal SET name = ?, price = ?, category_id = ?, type_id = ?, description = ?, image_path = ? " +
                      "WHERE meal_id = ?";
                System.out.println("[FoodMenuDialogController] UPDATE mode - Editing meal ID: " + editingMeal.getMealId());
            }
            
            try(PreparedStatement ps = conn.prepareStatement(sql))
            {
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setInt(3, category_id);
                ps.setInt(4, type_id);
                ps.setString(5, description);
                ps.setString(6, imagePath != null ? imagePath : "");
                
                if (editingMeal != null) {
                    ps.setInt(7, editingMeal.getMealId());  // Add meal_id for UPDATE
                }
                
                int result = ps.executeUpdate();
                System.out.println("[FoodMenuDialogController] Rows affected: " + result);
                
                if (result > 0) {
                    System.out.println("[FoodMenuDialogController] SUCCESS - Meal saved! Image path: " + imagePath);
                } else {
                    System.out.println("[FoodMenuDialogController] ERROR - No rows were updated");
                }
                
                okayButton.getScene().getWindow().hide();
            }
        }
        catch (SQLException e)
        {
            System.out.println("[FoodMenuDialogController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel()
    {
        cancelButton.getScene().getWindow().hide();
    }
}