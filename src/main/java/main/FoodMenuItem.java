package main;

public class FoodMenuItem {
    int mealId;
    String name;
    double price;
    String categoryName;
    String typeName;
    String description;
    String imagePath;

    public FoodMenuItem(){}

    public FoodMenuItem(int mealId, String name, double price, String categoryName, String typeName, String description, String imagePath)
    {
        this.mealId = mealId;
        this.name = name;
        this.price = price;
        this.categoryName = categoryName;
        this.typeName = typeName;
        this.description = description;
        this.imagePath = imagePath;
    }

    // Getters

    public int getMealId() {
        return mealId;
    }

    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }
    
    // Setters

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
