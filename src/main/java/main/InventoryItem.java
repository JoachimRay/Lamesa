package main;

// POJO class: holds data for one inventory row

public class InventoryItem {

    // Fields
    private int id;
    private String productName;
    private String category;
    private String type;
    private String instruction;
    private int stockQuantity;
    private String status;
    private String dateAdded;
    private boolean selected;

    // Constructor
    public InventoryItem(int id, String productName, String category, String type, String instruction, int stockQuantity, String status, String dateAdded) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.type = type;
        this.instruction = instruction;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.dateAdded = dateAdded;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getInstruction() {
        return instruction;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public String getStatus() {
        return status;
    }

    // Setters

    public void setId(int id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
