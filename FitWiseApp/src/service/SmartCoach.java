package service; import model.User;
public class SmartCoach {
 public static String getDailyInsight(User u,double bmi,int steps,int water){if(steps<u.getDailyStepGoal())return "Movement is below target. A 15–20 minute walk can close the gap.";if(water<(int)(u.getWeight()*33*.65))return "Hydration is behind target. Add a glass of water before your next meal.";if("Muscle Building".equals(u.getFitnessGoal()))return "Power session today: prioritize protein, progressive overload and recovery.";if("Weight Loss".equals(u.getFitnessGoal()))return "Stay near your calorie target and keep daily movement consistent.";return "Strong consistency today. Keep meals balanced and protect recovery time.";}
 public static int score(User u,int steps,int water){int s=50;if(steps>=u.getDailyStepGoal())s+=20;if(water>=u.getWeight()*33*.8)s+=15;if("Maintain Weight".equals(u.getFitnessGoal()))s+=15;return Math.min(100,s);}
}
