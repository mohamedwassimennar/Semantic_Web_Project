package tn.sem.websem;

public class FoodDto {
    private String food;    // URI of the food
    private String foodType;   // Type of food
    private String quantity;     // Quantity of food
    private String expiryDate;   // Expiry date of food

    // Constructors
    public FoodDto() {
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
