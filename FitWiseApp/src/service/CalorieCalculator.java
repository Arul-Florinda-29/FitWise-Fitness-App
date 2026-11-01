package service;

public class CalorieCalculator {
    private static void validate(double w, double h, int age) {
        if (!Double.isFinite(w) || !Double.isFinite(h) || w <= 0 || h <= 0 || age < 13 || age > 120)
            throw new IllegalArgumentException("Enter valid positive weight/height and an age between 13 and 120.");
    }
    public static double calculateBMR(double w, double h, int age, String g) {
        validate(w,h,age);
        double base = 10*w + 6.25*h - 5*age;
        if ("Male".equalsIgnoreCase(g)) return base + 5;
        if ("Female".equalsIgnoreCase(g)) return base - 161;
        // The Mifflin-St Jeor equation has sex-specific constants; use their midpoint
        // when the profile does not specify male or female.
        return base - 78;
    }
    public static double activityMultiplier(String a) {
        if ("Lightly Active".equalsIgnoreCase(a)) return 1.375;
        if ("Moderately Active".equalsIgnoreCase(a)) return 1.55;
        if ("Very Active".equalsIgnoreCase(a)) return 1.725;
        if ("Extra Active".equalsIgnoreCase(a)) return 1.9;
        return 1.2;
    }
    public static double calculateTDEE(double w,double h,int age,String g,String a) {
        return calculateBMR(w,h,age,g)*activityMultiplier(a);
    }
    public static int calculateDailyCalorieTarget(double w,double h,int age,String g,String a,String goal) {
        double t=calculateTDEE(w,h,age,g,a);
        if ("Weight Loss".equals(goal)) t-=300;
        else if ("Weight Gain".equals(goal)) t+=300;
        else if ("Muscle Building".equals(goal)) t+=200;
        // Avoid an arbitrary 1200 kcal floor; do not prescribe below estimated BMR.
        return (int)Math.round(Math.max(calculateBMR(w,h,age,g), t));
    }
    public static double calculateWaterIntakeLitres(double w) {
        if (!Double.isFinite(w) || w <= 0) throw new IllegalArgumentException("Weight must be positive.");
        return Math.round(w*33)/1000.0;
    }
    public static int calculateProteinTarget(double w,String goal) {
        if (!Double.isFinite(w) || w <= 0) throw new IllegalArgumentException("Weight must be positive.");
        double factor="Muscle Building".equals(goal)?1.8:("Weight Loss".equals(goal)?1.6:1.2);
        return (int)Math.round(w*factor);
    }
}
