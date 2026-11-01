package service;

import model.*;
import data.DataStore;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Builds a calorie-targeted meal plan for a user, choosing real items from the
 * food catalog so that:
 *  - the plan changes from day to day (no more identical menu every day,
 *    including weekends),
 *  - only foods matching the user's diet preference are offered
 *    (Vegan / Vegetarian / Eggetarian / Non-Vegetarian), and
 *  - foods that clash with the user's stated allergies are excluded.
 */
public class DietRecommendation implements Recommendation {

    private static final String[] BREAKFAST_POOL =
            {"Oats", "Egg", "Banana", "Greek Yogurt", "Almonds", "Peanut Butter", "Milk"};
    private static final String[] MAIN_POOL =
            {"Brown Rice", "Chicken Breast", "Paneer", "Grilled Fish", "Tofu", "Lentils (Dal)", "Sweet Potato", "Broccoli"};
    private static final String[] SNACK_POOL =
            {"Banana", "Almonds", "Greek Yogurt", "Mixed Vegetable Salad", "Peanut Butter"};

    public String getPlanType() { return "Adaptive Meal Plan"; }

    /** Is this food type allowed for the user's stated food preference? */
    private boolean dietCompatible(String foodDiet, String pref) {
        if ("Non-Vegetarian".equals(pref)) return true;
        if ("Eggetarian".equals(pref)) return !"Non-Vegetarian".equals(foodDiet);
        if ("Vegan".equals(pref)) return "Vegan".equals(foodDiet);
        return "Vegetarian".equals(foodDiet) || "Vegan".equals(foodDiet); // Vegetarian
    }

    /**
     * Conservative name-based allergy/avoid filtering. The catalog stores food names,
     * not verified ingredient labels, so this can screen obvious conflicts but cannot
     * certify that a meal is allergen-free.
     */
    private boolean allergySafe(String foodName, String allergies) {
        if (allergies == null || allergies.trim().isEmpty() || allergies.trim().equalsIgnoreCase("none")) return true;
        String name = foodName.toLowerCase(Locale.ROOT);
        for (String raw : allergies.toLowerCase(Locale.ROOT).split("[,;/]+")) {
            String t = raw.trim();
            if (t.isEmpty()) continue;
            if (name.contains(t)) return false;

            boolean dairy = t.contains("dairy") || t.contains("lactose") || t.equals("milk") || t.equals("casein");
            if (dairy && containsAny(name, "milk", "paneer", "yogurt", "cheese", "butter", "ghee", "whey")) return false;

            boolean nut = t.contains("nut") || t.contains("peanut") || t.contains("almond") || t.contains("cashew") || t.contains("walnut");
            if (nut && containsAny(name, "peanut", "almond", "cashew", "walnut", "mixed nuts")) return false;

            boolean gluten = t.contains("gluten") || t.contains("wheat") || t.contains("barley") || t.contains("rye");
            if (gluten && containsAny(name, "oats", "wheat", "bread", "roti", "pasta", "barley", "rye")) return false;

            if ((t.contains("egg") || t.contains("eggs")) && containsAny(name, "egg", "omelette", "mayonnaise")) return false;
            if ((t.contains("fish") || t.contains("seafood") || t.contains("shellfish"))
                    && containsAny(name, "fish", "prawn", "shrimp", "crab", "seafood")) return false;
            if ((t.equals("soy") || t.contains("soybean") || t.contains("soya"))
                    && containsAny(name, "tofu", "soy", "soya")) return false;
            if ((t.contains("sesame") || t.equals("til")) && containsAny(name, "sesame", "tahini", "til")) return false;
        }
        return true;
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }

    private List<Food> filteredPool(String[] names, User u) {
        Map<String, Food> byName = new LinkedHashMap<>();
        for (Food f : DataStore.getAllFoods()) byName.put(f.getFoodName(), f);
        List<Food> pool = new ArrayList<>();
        for (String n : names) {
            Food f = byName.get(n);
            if (f == null) continue;
            if (!dietCompatible(f.getDietType(), u.getFoodPreference())) continue;
            if (!allergySafe(f.getFoodName(), u.getAllergies())) continue;
            pool.add(f);
        }
        if (pool.isEmpty()) {
            // Guarantee a plan can always be produced: fall back to any safe food in the full catalog.
            for (Food f : DataStore.getAllFoods()) {
                if (dietCompatible(f.getDietType(), u.getFoodPreference()) && allergySafe(f.getFoodName(), u.getAllergies())) {
                    pool.add(f);
                }
            }
        }
        return pool;
    }

    private Food pick(List<Food> pool, int seed) {
        if (pool.isEmpty()) return new Food("Balanced whole-food meal", 0, 0, 0, 0, "Vegetarian");
        return pool.get(Math.floorMod(seed, pool.size()));
    }

    /**
     * Builds a plan for one specific day. dayIndex is any stable integer that changes
     * from day to day (we use day-of-year) so consecutive days differ, but the same
     * calendar date always produces the same plan if reopened.
     */
    public List<MealPlan> getDailyPlanForDay(User u, int dayIndex) {
        int t = getPersonalizedCalorieTarget(u);
        int b = (int) (t * .25), l = (int) (t * .35), s = (int) (t * .15), d = t - b - l - s;

        List<Food> breakfastPool = filteredPool(BREAKFAST_POOL, u);
        List<Food> mainPool = filteredPool(MAIN_POOL, u);
        List<Food> snackPool = filteredPool(SNACK_POOL, u);

        int seed = dayIndex + u.getUserId() * 3;
        Food breakfast = pick(breakfastPool, seed);
        Food lunch = pick(mainPool, seed + 1);
        Food dinner = pick(mainPool, seed + 2);
        if (mainPool.size() > 1 && dinner.getFoodName().equals(lunch.getFoodName())) {
            dinner = pick(mainPool, seed + 3);
        }
        Food snack = pick(snackPool, seed + 4);

        // Weekly experience changes actual meal choices, not just the summary.
        model.WeeklyAdaptation adaptation = new AdaptiveRecommendationEngine().getCurrent(u);
        if (adaptation != null && "SIMPLER_MEALS".equals(adaptation.getDietStrategy())) {
            breakfast = pickNamedOrFallback(breakfastPool, "Oats", breakfast);
            lunch = pickNamedOrFallback(mainPool, "Lentils (Dal)", lunch);
            dinner = pickNamedOrFallback(mainPool, "Brown Rice", dinner);
            snack = pickNamedOrFallback(snackPool, "Banana", snack);
        } else if (adaptation != null && "MORE_SATIATING".equals(adaptation.getDietStrategy())) {
            snack = pickNamedOrFallback(snackPool, "Greek Yogurt", snack);
            if ("Vegan".equals(u.getFoodPreference())) {
                snack = pickNamedOrFallback(snackPool, "Peanut Butter", snack);
                lunch = pickNamedOrFallback(mainPool, "Tofu", lunch);
            } else {
                lunch = pickNamedOrFallback(mainPool, "Paneer", lunch);
                if ("Non-Vegetarian".equals(u.getFoodPreference())) lunch = pickNamedOrFallback(mainPool, "Chicken Breast", lunch);
            }
        }

        List<MealPlan> r = new ArrayList<>();
        r.add(new MealPlan("Breakfast", breakfast.getFoodName() + " with fruit", b));
        r.add(new MealPlan("Lunch", lunch.getFoodName() + " with brown rice & vegetables", l));
        r.add(new MealPlan("Smart Snack", snack.getFoodName(), s));
        r.add(new MealPlan("Dinner", dinner.getFoodName() + " with steamed vegetables", Math.max(200, d)));
        return r;
    }

    private Food pickNamedOrFallback(List<Food> pool, String name, Food fallback) {
        for (Food f : pool) if (f.getFoodName().equalsIgnoreCase(name)) return f;
        return fallback;
    }

    public List<MealPlan> getDailyPlan(User u) {
        return getDailyPlanForDay(u, LocalDate.now().getDayOfYear());
    }

    public int getTotalCalories(List<MealPlan> p) {
        int t = 0;
        for (MealPlan m : p) t += m.getCalories();
        return t;
    }

    /** A genuine 7-day plan for the current week: each day is different and matches getDailyPlan() for "today". */
    public Map<String, List<MealPlan>> getWeeklyPlan(User u) {
        Map<String, List<MealPlan>> m = new LinkedHashMap<>();
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            String dow = date.getDayOfWeek().toString();
            String label = dow.charAt(0) + dow.substring(1).toLowerCase() + "  •  " + date;
            m.put(label, getDailyPlanForDay(u, date.getDayOfYear()));
        }
        return m;
    }

    /** Returns safe, diet-compatible alternatives for the selected meal slot. */
    public List<Food> getAlternativeFoods(User u, String mealType, int dayIndex, String currentDescription) {
        String[] source;
        if ("Breakfast".equalsIgnoreCase(mealType)) source = BREAKFAST_POOL;
        else if ("Smart Snack".equalsIgnoreCase(mealType)) source = SNACK_POOL;
        else source = MAIN_POOL;

        List<Food> pool = filteredPool(source, u);
        String current = currentDescription == null ? "" : currentDescription.toLowerCase();
        List<Food> result = new ArrayList<>();
        for (Food f : pool) {
            if (!current.contains(f.getFoodName().toLowerCase())) result.add(f);
        }
        if (result.size() < 3) {
            for (Food f : DataStore.getAllFoods()) {
                if (dietCompatible(f.getDietType(), u.getFoodPreference())
                        && allergySafe(f.getFoodName(), u.getAllergies())
                        && !current.contains(f.getFoodName().toLowerCase())
                        && !result.contains(f)) result.add(f);
            }
        }
        Collections.rotate(result, Math.floorMod(dayIndex + u.getUserId(), Math.max(1, result.size())));
        return result;
    }

    /** Explains why the engine selected the current plan. */
    public String getPersonalizationSummary(User u) {
        double bmi = BMICalculator.calculateBMI(u.getWeight(), u.getHeight());
        String category = BMICalculator.getCategory(bmi);
        if ("Underweight".equals(category))
            return "BMI " + String.format("%.1f", bmi) + " • Underweight: calorie surplus + nutrient-dense meals";
        if ("Normal".equals(category))
            return "BMI " + String.format("%.1f", bmi) + " • Normal: balanced calories + recovery-focused meals";
        if ("Overweight".equals(category))
            return "BMI " + String.format("%.1f", bmi) + " • Overweight: controlled calories + high-fibre meals";
        return "BMI " + String.format("%.1f", bmi) + " • Obese: controlled calories + high-fibre meals";
    }

    public String getAdaptiveSummary(User u){
        String summary=new AdaptiveRecommendationEngine().getSummary(u);
        return "<html><b>Weekly experience-based adaptation</b><br>"+summary.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>")+"</html>";
    }

    public int getPersonalizedCalorieTarget(User u) {
        int t = CalorieCalculator.calculateDailyCalorieTarget(
                u.getWeight(), u.getHeight(), u.getAge(), u.getGender(), u.getActivityLevel(), u.getFitnessGoal());
        double bmi = BMICalculator.calculateBMI(u.getWeight(), u.getHeight());
        String category = BMICalculator.getCategory(bmi);
        if (("Maintain Weight".equals(u.getFitnessGoal()) || "General Fitness".equals(u.getFitnessGoal()))) {
            if ("Underweight".equals(category)) t += 250;
            else if ("Overweight".equals(category) || "Obese".equals(category)) t -= 250;
        }
        // Apply only the bounded calorie change from the most recent weekly review.
        model.WeeklyAdaptation adaptation = new AdaptiveRecommendationEngine().getCurrent(u);
        if (adaptation != null) t += adaptation.getCalorieAdjustment();
        // Avoid aggressive changes; the experience engine only makes small adjustments.
        return Math.max(1, t);
    }
}
