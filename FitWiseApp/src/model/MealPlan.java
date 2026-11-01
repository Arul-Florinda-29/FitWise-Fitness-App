package model;

public class MealPlan {
    private String mealType;
    private String description;
    private int calories;

    public MealPlan() {
    }

    public MealPlan(String mealType, String description, int calories) {
        this.mealType = mealType;
        this.description = description;
        this.calories = calories;
    }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
}
