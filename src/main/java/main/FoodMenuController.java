package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FoodMenuController {

    private List<FoodMenuItem> allMeals = new ArrayList<>();


    @FXML
    private Button addNewButton;

    @FXML
    private ToggleButton appetizerToggle;

    @FXML
    private ToggleButton breakfastToggle;

    @FXML
    private ToggleButton dessertToggle;

    @FXML
    private ToggleButton dinnerToggle;

    @FXML
    private ToggleButton drinksToggle;

    @FXML
    private FlowPane foodCardsPane;

    @FXML
    private ToggleButton lunchToggle;

    @FXML
    private ToggleButton nonVegetarianToggle;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField searchField;

    @FXML
    private ToggleButton vegetarianToggle;

    @FXML
    private void initialize()
    {
        appetizerToggle.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        breakfastToggle.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        lunchToggle.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dinnerToggle.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dessertToggle.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        drinksToggle.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        vegetarianToggle.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        nonVegetarianToggle.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        loadFoodCards();
    }

    private boolean matchesSelectedCategory(String categoryName) 
    {
        if (categoryName == null)
            return false;

        // Categories from meal_category: Breakfast, Lunch, Dinner, Snack, Dessert
        if(breakfastToggle.isSelected() && categoryName.equalsIgnoreCase("Breakfast"))
            return true;

        if(lunchToggle.isSelected() && categoryName.equalsIgnoreCase("Lunch"))
            return true;

        if(dinnerToggle.isSelected() && categoryName.equalsIgnoreCase("Dinner"))
            return true;

        if(drinksToggle.isSelected() && categoryName.equalsIgnoreCase("Snack"))
            return true;

        if(dessertToggle.isSelected() && categoryName.equalsIgnoreCase("Dessert"))
            return true;

        if(appetizerToggle.isSelected() && categoryName.equalsIgnoreCase("Appetizer"))
            return true;

        return false;
    }

    private boolean matchesSelectedType(String typeName)
    {
        if (typeName == null || typeName.isEmpty())
            return false;

        String normalizedType = typeName.toLowerCase().trim();
        
        // Types from meal_types: Vegetarian, Non-Vegetarian, Vegan, Gluten-Free, High-Protein
        if(vegetarianToggle.isSelected() && normalizedType.equalsIgnoreCase("Vegetarian"))
            return true;

        if(nonVegetarianToggle.isSelected() && normalizedType.equalsIgnoreCase("Non-Vegetarian"))
            return true;

        return false;
    }

    private VBox createFoodCard(FoodMenuItem meal)
    {
        VBox card = new VBox(8);
        card.getStyleClass().add("food-card");
        card.setPrefWidth(280);
        card.setPrefHeight(350);
        card.setMinHeight(350);
        card.setMaxHeight(350);

        // Image container with fixed height
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        imageContainer.setMinHeight(180);
        imageContainer.setMaxHeight(180);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("food-card-image");

        // Load the image if path exists
        if (meal.getImagePath() != null && !meal.getImagePath().isEmpty()) 
            {
            try {
                String imagePath = meal.getImagePath();
                if (!imagePath.startsWith("assets/")) {
                    imagePath = "assets/" + imagePath;
                }
                Image image = new Image(getClass().getResourceAsStream("/" + imagePath));
                if (image != null && !image.isError()) {
                    imageView.setImage(image);
                }
            } catch (Exception e) {
                System.out.println("[FoodMenuController] Could not load image: " + meal.getImagePath());
            }
        }

        // Title
        Label nameLabel = new Label(meal.getName());
        nameLabel.getStyleClass().add("food-card-title");
        nameLabel.setWrapText(true);

        // Description
        Label descLabel = new Label(meal.getDescription() != null ? meal.getDescription() : "");
        descLabel.getStyleClass().add("food-card-description");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);

        // Vertical spacer to push bottom row down
        Region verticalSpacer = new Region();
        VBox.setVgrow(verticalSpacer, Priority.ALWAYS);

        // Bottom row with Edit link, Delete link, and Price
        HBox bottomRow = new HBox(8);
        bottomRow.getStyleClass().add("food-card-bottom");
        
        Label editLabel = new Label("Edit");
        editLabel.getStyleClass().add("food-card-edit");
        editLabel.setOnMouseClicked(event -> handleEditMeal(meal));

        Label deleteLabel = new Label("Delete");
        deleteLabel.getStyleClass().add("food-card-delete");
        deleteLabel.setOnMouseClicked(event -> handleDeleteMeal(meal));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label priceLabel = new Label(String.format("₱%.0f", meal.getPrice()));
        priceLabel.getStyleClass().add("food-card-price");
        
        bottomRow.getChildren().addAll(editLabel, deleteLabel, spacer, priceLabel);

        imageContainer.getChildren().add(imageView);
        card.getChildren().addAll(imageContainer, nameLabel, descLabel, verticalSpacer, bottomRow);

        return card;
    }

    // Handle editing a meal
    private void handleEditMeal(FoodMenuItem meal) 
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/food_menu_dialog.fxml"));
            Parent content = loader.load();
            
            // Get the controller and set it to edit mode
            FoodMenuDialogController controller = loader.getController();
            controller.setEditMode(meal);
            
            Stage dialog = new Stage();
            dialog.setTitle("Edit Food");
            dialog.setScene(new Scene(content));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();
            
            loadFoodCards();  // Refresh after edit
            
        } catch (Exception e) {
            System.out.println("[FoodMenuController] ERROR opening edit dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handle deleting a meal
    private void handleDeleteMeal(FoodMenuItem meal)
    {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Food Item");
        alert.setHeaderText("Delete \"" + meal.getName() + "\"?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
        {
            String dbUrl = "jdbc:sqlite:database/lamesa.db";
            String sql = "DELETE FROM meal WHERE meal_id = ?";

            try (Connection conn = DriverManager.getConnection(dbUrl);
                 PreparedStatement ps = conn.prepareStatement(sql))
            {
                ps.setInt(1, meal.getMealId());
                int rowsDeleted = ps.executeUpdate();
                System.out.println("[FoodMenuController] Deleted meal: " + meal.getName() + " (rows: " + rowsDeleted + ")");
                
                loadFoodCards();  // Refresh after delete
            }
            catch (SQLException e)
            {
                System.out.println("[FoodMenuController] ERROR deleting meal: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void applyFilters()
    {
        foodCardsPane.getChildren().clear();

        String searchText = searchField.getText().toLowerCase();    // Get text on searcg field

        boolean anyCategorySelected = appetizerToggle.isSelected() || breakfastToggle.isSelected() ||
                                      lunchToggle.isSelected() || dinnerToggle.isSelected() ||
                                      dessertToggle.isSelected() || drinksToggle.isSelected();

        boolean anyTypeSelected = vegetarianToggle.isSelected() || nonVegetarianToggle.isSelected();

        System.out.println("[FILTER DEBUG] anyCategorySelected: " + anyCategorySelected + " | anyTypeSelected: " + anyTypeSelected);
        System.out.println("[FILTER DEBUG] Vegetarian: " + vegetarianToggle.isSelected() + " | Non-Veg: " + nonVegetarianToggle.isSelected());

        for (FoodMenuItem meal : allMeals)
        {
            boolean searchMatch = searchText.isEmpty() || meal.getName().toLowerCase().contains(searchText);
            boolean categoryMatch = !anyCategorySelected || matchesSelectedCategory(meal.getCategoryName());
            boolean typeMatch = !anyTypeSelected || matchesSelectedType(meal.getTypeName());

            if (searchMatch && categoryMatch && typeMatch)
            {
                VBox card = createFoodCard(meal);
                foodCardsPane.getChildren().add(card);
            }
            else if (anyTypeSelected)
            {
                System.out.println("[FILTER DEBUG] " + meal.getName() + " - Type: " + meal.getTypeName() + " | typeMatch: " + typeMatch);
            }
        }

        System.out.println("[FoodMenuController] Showing " + foodCardsPane.getChildren().size() + " cards");
    }

    private void loadFoodCards()
    {
        allMeals.clear();

        String dbUrl = "jdbc:sqlite:database/lamesa.db";
        // Note: m.category_id references meal_category (meal times), m.type_id references meal_types (dietary types)
        String sql = "SELECT m.meal_id, m.name, m.price, m.description, m.image_path, c.category_name AS category_name, t.type_name AS type_name " + 
                     "FROM meal m " + "LEFT JOIN meal_category c ON m.category_id = c.category_id " +
                     "LEFT JOIN meal_types t ON m.type_id = t.type_id " + "ORDER BY m.name";
        
        try(Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) 
            {
                while(rs.next())
                {
                    FoodMenuItem meal = new FoodMenuItem(
                        rs.getInt("meal_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category_name"),
                        rs.getString("type_name"),
                        rs.getString("description"),
                        rs.getString("image_path")
                    );
                    allMeals.add(meal);
                }

                System.out.println("[FoodMenuController] Loaded " + allMeals.size() + " meals");
            }

            catch (SQLException e) 
            {
                System.out.println("[FoodMenuController] ERROR loading meals: " + e.getMessage());
                e.printStackTrace();
            }

            applyFilters(); // Display the loaded meals
    }

    @FXML
    private void handleAddNew()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/food_menu_dialog.fxml"));
            Parent content = loader.load();
            Stage dialog = new Stage();
            dialog.setScene(new Scene(content));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

            loadFoodCards(); // Refresh after dialog closes
        }
        catch (Exception e) 
        {
            System.out.println("[FoodMenuController] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}