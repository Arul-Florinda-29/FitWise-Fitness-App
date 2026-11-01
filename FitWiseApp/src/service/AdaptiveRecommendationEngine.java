package service;

import data.DataStore;
import model.ExperienceLearningModel;
import model.Progress;
import model.User;
import model.WeeklyAdaptation;

import java.time.LocalDate;
import java.util.List;

/**
 * Personalized online-learning engine. Each weekly review evaluates the previous
 * week's plan using that user's recorded adherence, progress and feedback. It
 * updates a per-user reward model and uses the learned scores for the next plan.
 * Safety constraints (especially reported discomfort/low energy) override learning.
 */
public class AdaptiveRecommendationEngine {
    public static final String EASY="Easy", MODERATE="Moderate", HARD="Hard";
    public static final String LOW="Low", OK="Okay", HIGH="High";
    private static final String[] WORKOUTS={"BALANCED","PROGRESS","SHORTER","RECOVERY","LOW_IMPACT"};
    private static final String[] DIETS={"BALANCED","MORE_SATIATING","SIMPLER_MEALS","SUPPORT_INTAKE","MAINTAIN_INTAKE","REVIEW_PORTIONS"};

    public WeeklyAdaptation reviewAndSave(User u, String difficulty, String energy, String hunger, String mealSatisfaction, String discomfort) {
        LocalDate today=LocalDate.now(), start=today.minusDays(6);
        int workoutDays=0, mealsLogged=0, mealsEaten=0, sleepLogged=0; double sleepSum=0;
        for(int i=0;i<7;i++){
            String d=start.plusDays(i).toString();
            if(DataStore.getWorkoutCompletedCount(u.getUserId(),d)>0) workoutDays++;
            int logged=DataStore.getMealsLogged(u.getUserId(),d);
            if(logged>0){mealsLogged+=logged;mealsEaten+=DataStore.getMealsEaten(u.getUserId(),d);}
            double sl=DataStore.getSleep(u.getUserId(),d); if(sl>0){sleepLogged++;sleepSum+=sl;}
        }
        double workoutAdherence=workoutDays/7.0;
        double mealAdherence=mealsLogged==0?.5:(double)mealsEaten/mealsLogged;
        double avgSleep=sleepLogged==0?0:sleepSum/sleepLogged;
        double weightChange=weightChange(u.getUserId(),start,today);
        ExperienceLearningModel learner=DataStore.getExperienceLearningModel(u.getUserId());
        WeeklyAdaptation previous=DataStore.getWeeklyAdaptation(u.getUserId());
        StringBuilder why=new StringBuilder("Weekly review ").append(today).append(": ");

        // The current week's outcomes are attributed to the previous week's plan.
        if(previous!=null){
            double workoutReward=workoutReward(workoutAdherence,energy,difficulty,discomfort,avgSleep);
            double dietReward=dietReward(mealAdherence,mealSatisfaction,hunger,weightChange,u.getFitnessGoal());
            learner.learnWorkout(previous.getWorkoutStrategy(),workoutReward);
            learner.learnDiet(previous.getDietStrategy(),dietReward);
            why.append(String.format("Online model updated from your previous plan (workout reward %.2f, diet reward %.2f). ",workoutReward,dietReward));
        } else {
            why.append("First review creates a baseline; the model starts learning after the next review measures this plan's outcomes. ");
        }

        String ruleWorkout=ruleWorkout(difficulty,energy,discomfort,avgSleep,workoutAdherence);
        String ruleDiet=ruleDiet(hunger,mealSatisfaction,mealAdherence,weightChange,u.getFitnessGoal());
        String workout=selectWorkout(learner,ruleWorkout,difficulty,energy,discomfort,avgSleep);
        String diet=selectDiet(learner,ruleDiet,hunger,mealSatisfaction,energy,mealAdherence);
        int calorieAdjustment=calorieAdjustment(diet,weightChange,u.getFitnessGoal(),energy);
        why.append("Next workout strategy: ").append(workout).append("; diet strategy: ").append(diet).append(". ");
        why.append(String.format("Observed workout completion %.0f%%, meal adherence %.0f%%, average sleep %.1f h, weight change %+.2f kg. ",workoutAdherence*100,mealAdherence*100,avgSleep,weightChange));
        why.append(learner.summary());
        WeeklyAdaptation a=new WeeklyAdaptation(u.getUserId(),today.toString(),workout,diet,why.toString(),difficulty,energy,hunger,mealSatisfaction,discomfort,workoutAdherence,mealAdherence,weightChange,calorieAdjustment);
        DataStore.saveExperienceLearningModel(u.getUserId(),learner);
        DataStore.saveWeeklyAdaptation(a);
        return a;
    }

    private double workoutReward(double adherence,String energy,String difficulty,String discomfort,double sleep){
        double r=.55*clamp(adherence/.70);
        r+=.15*("High".equals(energy)?.9:"Okay".equals(energy)?.7:.25);
        r+=.15*("Hard".equals(difficulty)?.25:"Moderate".equals(difficulty)?.75:.9);
        r+=.15*("Yes".equalsIgnoreCase(discomfort)?0:(sleep>0&&sleep<6?.35:.9));
        return clamp(r);
    }
    private double dietReward(double adherence,String satisfaction,String hunger,double weightChange,String goal){
        double r=.50*clamp(adherence/.75);
        r+=.25*("Good".equalsIgnoreCase(satisfaction)?.95:"Okay".equalsIgnoreCase(satisfaction)?.7:.2);
        r+=.15*("High".equals(hunger)?.25:"Low".equals(hunger)?.85:.75);
        double trend=.65;
        if("Weight Loss".equals(goal)) trend=weightChange<-.1&&weightChange>-.75?.95:weightChange<-.75?.55:weightChange>.5?.25:.65;
        else if("Weight Gain".equals(goal)||"Muscle Building".equals(goal)) trend=weightChange>.1&&weightChange<.75?.95:weightChange<-.5?.25:.65;
        else if("Maintain Weight".equals(goal)) trend=Math.abs(weightChange)<=.5?.95:.55;
        r+=.10*trend;
        return clamp(r);
    }
    private String ruleWorkout(String difficulty,String energy,String discomfort,double sleep,double adherence){
        if("Yes".equalsIgnoreCase(discomfort))return "LOW_IMPACT";
        if(HARD.equals(difficulty)||LOW.equals(energy)||(sleep>0&&sleep<6))return "RECOVERY";
        if(adherence<.35)return "SHORTER";
        if(EASY.equals(difficulty)&&adherence>=.70&&!LOW.equals(energy))return "PROGRESS";
        return "BALANCED";
    }
    private String ruleDiet(String hunger,String satisfaction,double adherence,double weightChange,String goal){
        if(HIGH.equals(hunger))return "MORE_SATIATING";
        if("Poor".equalsIgnoreCase(satisfaction)||adherence<.5)return "SIMPLER_MEALS";
        if(weightChange<-.75&&("Weight Gain".equals(goal)||"Muscle Building".equals(goal)))return "SUPPORT_INTAKE";
        if(weightChange<-.75&&"Weight Loss".equals(goal))return "MAINTAIN_INTAKE";
        if(weightChange>.75&&"Weight Loss".equals(goal))return "REVIEW_PORTIONS";
        return "BALANCED";
    }
    private String selectWorkout(ExperienceLearningModel m,String baseline,String difficulty,String energy,String discomfort,double sleep){
        if("Yes".equalsIgnoreCase(discomfort))return "LOW_IMPACT";
        if(LOW.equals(energy)||(sleep>0&&sleep<5.5)||HARD.equals(difficulty))return "RECOVERY";
        if(m.workoutSamples()<1)return baseline;
        String best=baseline; double score=m.workoutScore(baseline)+.08;
        for(String s:WORKOUTS){if(m.workoutCount(s)>0){double candidate=m.workoutScore(s);if(s.equals(baseline))candidate+=.08;if(candidate>score){score=candidate;best=s;}}}
        return best;
    }
    private String selectDiet(ExperienceLearningModel m,String baseline,String hunger,String satisfaction,String energy,double adherence){
        // Safety/comfort constraints cannot be overridden by learned reward scores.
        if(HIGH.equals(hunger))return "MORE_SATIATING";
        if("Poor".equalsIgnoreCase(satisfaction)||adherence<.35)return "SIMPLER_MEALS";
        if(LOW.equals(energy))return "MAINTAIN_INTAKE";
        if(m.dietSamples()<1)return baseline;
        String best=baseline; double score=m.dietScore(baseline)+.08;
        for(String s:DIETS){if(m.dietCount(s)>0){double candidate=m.dietScore(s);if(s.equals(baseline))candidate+=.08;if(candidate>score){score=candidate;best=s;}}}
        return best;
    }
    private int calorieAdjustment(String diet,double weightChange,String goal,String energy){
        int adjustment=0;
        if("SUPPORT_INTAKE".equals(diet))adjustment=100;
        else if("MORE_SATIATING".equals(diet))adjustment=50;
        else if("REVIEW_PORTIONS".equals(diet))adjustment=-50;
        if(LOW.equals(energy))adjustment=Math.max(0,adjustment);
        return Math.max(-100,Math.min(150,adjustment));
    }
    private double weightChange(int userId, LocalDate start, LocalDate end){
        List<Progress> p=DataStore.getProgressForUser(userId); Progress first=null,last=null;
        for(Progress x:p){try{LocalDate d=LocalDate.parse(x.getDate());if(!d.isBefore(start)&&!d.isAfter(end)){if(first==null)first=x;last=x;}}catch(Exception ignored){}}
        return first==null||last==null||first==last?0:last.getWeight()-first.getWeight();
    }
    private double clamp(double x){return Math.max(0,Math.min(1,x));}
    public WeeklyAdaptation getCurrent(User u){return DataStore.getWeeklyAdaptation(u.getUserId());}
    public String getSummary(User u){
        WeeklyAdaptation a=getCurrent(u);
        if(a==null)return "No weekly experience review saved yet. Record progress, then select WEEKLY REVIEW to personalize next week's plans.";
        ExperienceLearningModel m=DataStore.getExperienceLearningModel(u.getUserId());
        return "Last reviewed: "+a.getReviewDate()+"\n"+a.getExplanation()+"\nWorkout adjustment: "+displayWorkout(a.getWorkoutStrategy())+". Diet adjustment: "+displayDiet(a.getDietStrategy())+".\n"+m.summary();
    }
    public static String displayWorkout(String s){switch(s){case "LOW_IMPACT":return "low-impact, pain-free activity";case "RECOVERY":return "recovery-focused, lower intensity";case "PROGRESS":return "gradual progression";case "SHORTER":return "shorter sessions for consistency";default:return "balanced training";}}
    public static String displayDiet(String s){switch(s){case "MORE_SATIATING":return "more filling meals and review hunger";case "SIMPLER_MEALS":return "simpler meals and practical alternatives";case "SUPPORT_INTAKE":return "slightly higher intake for the goal";case "MAINTAIN_INTAKE":return "maintain intake and monitor recovery";case "REVIEW_PORTIONS":return "review portions and logging consistency";default:return "balanced meals";}}
}
