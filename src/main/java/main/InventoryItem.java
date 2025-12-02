package main;

public class InventoryItem {

    // Fields
    private int id;
    private String productName;
    private String category;
    private String type;
    private String instruction;
    private int stockQuantity;
    private String status;

    // Constructor
    public InventoryItem(int id, String productName, String category, String type, String instruction, int stockQuantity, String status) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.type = type;
        this.instruction = instruction;
        this.stockQuantity = stockQuantity;
        this.status = status;
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
}
