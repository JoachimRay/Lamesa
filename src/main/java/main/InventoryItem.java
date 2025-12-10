package main;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// POJO class: holds data for one inventory row

public class InventoryItem {

    // Fields as JavaFX Properties
    private final IntegerProperty id;
    private final StringProperty productName;
    private final StringProperty category;
    private final StringProperty type;
    private final StringProperty instruction;
    private final IntegerProperty stockQuantity;
    private final StringProperty status;

    // Constructor
    public InventoryItem(int id, String productName, String category, String type, String instruction, int stockQuantity, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.productName = new SimpleStringProperty(productName);
        this.category = new SimpleStringProperty(category);
        this.type = new SimpleStringProperty(type);
        this.instruction = new SimpleStringProperty(instruction);
        this.stockQuantity = new SimpleIntegerProperty(stockQuantity);
        this.status = new SimpleStringProperty(status);
    }

    // Property getters (required for JavaFX binding)
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public StringProperty instructionProperty() {
        return instruction;
    }

    public IntegerProperty stockQuantityProperty() {
        return stockQuantity;
    }

    public StringProperty statusProperty() {
        return status;
    }

    // Value getters
    public int getId() {
        return id.get();
    }

    public String getProductName() {
        return productName.get();
    }

    public String getCategory() {
        return category.get();
    }

    public String getType() {
        return type.get();
    }

    public String getInstruction() {
        return instruction.get();
    }

    public int getStockQuantity() {
        return stockQuantity.get();
    }

    public String getStatus() {
        return status.get();
    }

    // Setters

    public void setId(int id) {
        this.id.set(id);
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public void setInstruction(String instruction) {
        this.instruction.set(instruction);
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity.set(stockQuantity);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
}
