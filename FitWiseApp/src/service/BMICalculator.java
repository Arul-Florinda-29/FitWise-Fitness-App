package service;

public class BMICalculator {
    public static double calculateBMI(double weightKg, double heightCm) {
        if (!Double.isFinite(weightKg) || !Double.isFinite(heightCm)
                || weightKg <= 0 || heightCm <= 0) {
            throw new IllegalArgumentException("Weight and height must be positive numbers.");
        }
        double metres = heightCm / 100.0;
        return weightKg / (metres * metres);
    }
    public static String getCategory(double bmi) {
        if (!Double.isFinite(bmi) || bmi <= 0) throw new IllegalArgumentException("BMI must be a positive number.");
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }
    public static String getAutomaticGoal(double bmi) {
        if (bmi < 18.5) return "Weight Gain";
        if (bmi < 25) return "Maintain Weight";
        return "Weight Loss";
    }
    public static String getRecommendation(double bmi) {
        if (bmi < 18.5) return "Weight Gain — focus on nutritious meals and strength training.";
        if (bmi < 25) return "Maintain Weight — focus on balanced nutrition and regular activity.";
        return "Weight Loss — focus on a gradual, sustainable calorie deficit and regular activity.";
    }
}
