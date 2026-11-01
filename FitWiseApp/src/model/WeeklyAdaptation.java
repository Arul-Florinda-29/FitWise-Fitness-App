package model;

import java.io.Serializable;

/** Persisted result of a user's weekly experience review. */
public class WeeklyAdaptation implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int userId;
    private final String reviewDate, workoutStrategy, dietStrategy, explanation;
    private final String workoutDifficulty, energyLevel, hungerLevel, mealSatisfaction, discomfort;
    private final double workoutAdherence, mealAdherence, weightChangeKg;
    private final int calorieAdjustment;
    public WeeklyAdaptation(int userId, String reviewDate, String workoutStrategy, String dietStrategy, String explanation,
            String workoutDifficulty, String energyLevel, String hungerLevel, String mealSatisfaction, String discomfort,
            double workoutAdherence, double mealAdherence, double weightChangeKg, int calorieAdjustment) {
        this.userId=userId; this.reviewDate=reviewDate; this.workoutStrategy=workoutStrategy; this.dietStrategy=dietStrategy;
        this.explanation=explanation; this.workoutDifficulty=workoutDifficulty; this.energyLevel=energyLevel; this.hungerLevel=hungerLevel;
        this.mealSatisfaction=mealSatisfaction; this.discomfort=discomfort; this.workoutAdherence=workoutAdherence;
        this.mealAdherence=mealAdherence; this.weightChangeKg=weightChangeKg; this.calorieAdjustment=calorieAdjustment;
    }
    public int getUserId(){return userId;} public String getReviewDate(){return reviewDate;}
    public String getWorkoutStrategy(){return workoutStrategy;} public String getDietStrategy(){return dietStrategy;}
    public String getExplanation(){return explanation;} public String getWorkoutDifficulty(){return workoutDifficulty;}
    public String getEnergyLevel(){return energyLevel;} public String getHungerLevel(){return hungerLevel;}
    public String getMealSatisfaction(){return mealSatisfaction;} public String getDiscomfort(){return discomfort;}
    public double getWorkoutAdherence(){return workoutAdherence;} public double getMealAdherence(){return mealAdherence;}
    public double getWeightChangeKg(){return weightChangeKg;} public int getCalorieAdjustment(){return calorieAdjustment;}
}
