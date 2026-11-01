package service;

import model.*;
import java.util.*;
import model.WeeklyAdaptation;

public class WorkoutRecommendation implements Recommendation {
    public String getPlanType(){return "Adaptive Workout Plan";}

    public List<Workout> getWorkoutPlan(User u){
        List<Workout> p=new ArrayList<>();
        String g=u.getFitnessGoal();
        double bmi=BMICalculator.calculateBMI(u.getWeight(),u.getHeight());
        String category=BMICalculator.getCategory(bmi);
        String activity=u.getActivityLevel();

        if("Weight Loss".equals(g)){
            p.add(new Workout("Zone 2 Walk",g,"30 min • Moderate"));
            p.add(new Workout("Squat Circuit",g,"3 × 12 • Bodyweight"));
            p.add(new Workout("Push-up Circuit",g,"3 × 10 • Bodyweight"));
            p.add(new Workout("Reverse Lunges",g,"3 × 10 each leg"));
            p.add(new Workout("Core Plank",g,"3 × 40 sec"));
        }else if("Muscle Building".equals(g)){
            p.add(new Workout("Lower Body Strength",g,"4 × 8–10"));
            p.add(new Workout("Push Strength",g,"4 × 8–12"));
            p.add(new Workout("Pull / Back Work",g,"4 × 8–12"));
            p.add(new Workout("Core Stability",g,"3 × 45 sec"));
            p.add(new Workout("Mobility & Recovery",g,"10 min"));
        }else if("Weight Gain".equals(g)){
            p.add(new Workout("Full Body Strength",g,"4 × 8–10"));
            p.add(new Workout("Squats",g,"3 × 10"));
            p.add(new Workout("Push-ups",g,"3 × 10"));
            p.add(new Workout("Resistance Rows",g,"3 × 10"));
        }else{
            p.add(new Workout("Brisk Walk",g,"25 min"));
            p.add(new Workout("Mobility Flow",g,"10 min"));
            p.add(new Workout("Bodyweight Circuit",g,"3 rounds"));
            p.add(new Workout("Core Stability",g,"3 × 30 sec"));
        }

        // Profile analysis affects intensity and movement selection.
        if("Obese".equals(category)){
            p.add(0,new Workout("Low-Impact Warm-up",category,"8 min • Joint-friendly"));
            p.add(new Workout("Low-Impact Cardio",category,"15–20 min • Easy/Moderate"));
        }else if("Underweight".equals(category)){
            p.add(0,new Workout("Strength Warm-up",category,"8 min • Prepare for resistance work"));
        }else if("Very Active".equals(activity)){
            p.add(new Workout("Mobility & Recovery",category,"10–12 min"));
        }

        if("Yoga & Mobility".equals(u.getWorkoutPreference()))
            p.add(new Workout("Yoga Cool-down","Recovery","12 min"));
        if("Cardio".equals(u.getWorkoutPreference()))
            p.add(0,new Workout("Cardio Intervals","Cardio","20 min"));
        if("Home Training".equals(u.getWorkoutPreference()) && "Sedentary".equals(activity))
            p.add(0,new Workout("Gentle Warm-up","Home Training","8 min"));

        // The saved weekly review is the sole experience-based adaptation layer.
        WeeklyAdaptation adaptation = new AdaptiveRecommendationEngine().getCurrent(u);
        if (adaptation != null) {
            String strategy = adaptation.getWorkoutStrategy();
            if ("LOW_IMPACT".equals(strategy)) {
                p.clear();
                p.add(new Workout("Pain-Free Mobility","Weekly experience review","10 min • Stop any movement that causes pain"));
                p.add(new Workout("Easy Walk or Seated Cardio","Weekly experience review","10–15 min • Comfortable pace"));
            } else if ("RECOVERY".equals(strategy)) {
                p.clear();
                p.add(new Workout("Recovery Walk","Weekly experience review","10–15 min • Easy pace"));
                p.add(new Workout("Gentle Mobility","Weekly experience review","8–10 min • Comfortable range"));
                p.add(new Workout("Rest and Recovery","Weekly experience review","Prioritize sleep and rest"));
            } else if ("SHORTER".equals(strategy)) {
                p.clear();
                p.add(new Workout("Short Brisk Walk","Weekly experience review","10 min • Build consistency first"));
                p.add(new Workout("Simple Bodyweight Circuit","Weekly experience review","1–2 easy rounds"));
                p.add(new Workout("Gentle Mobility","Weekly experience review","5–8 min"));
            } else if ("PROGRESS".equals(strategy)) {
                p.add(0,new Workout("Progression Session","Weekly experience review","Add only 1–2 reps or 5 minutes if comfortable"));
            }
        }

        return p;
    }
}
