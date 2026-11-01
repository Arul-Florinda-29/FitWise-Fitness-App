package model;

public class Food {
    private int foodId;
    private String foodName;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private String dietType; // Vegetarian, Non-Vegetarian, Vegan, Eggetarian

    public Food() {
    }

    public Food(String foodName, double calories, double protein, double carbs, double fat, String dietType) {
        this.foodName = foodName;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.dietType = dietType;
    }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public String getDietType() { return dietType; }
    public void setDietType(String dietType) { this.dietType = dietType; }
}
