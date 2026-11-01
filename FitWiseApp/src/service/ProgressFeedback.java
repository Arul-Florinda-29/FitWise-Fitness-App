package service;

/**
 * Generates a short motivational/feedback message by comparing the most
 * recent progress entry against the previous one, in light of the user's
 * fitness goal. Simple rule-based logic - no AI/ML.
 */
public class ProgressFeedback {

    public static String getFeedback(String goal, double previousWeight, double latestWeight) {
        double diff = latestWeight - previousWeight;

        switch (goal) {
            case "Weight Loss":
                if (diff < -0.05) {
                    return "Great progress! You're trending toward your weight loss goal. Keep it up!";
                } else if (diff > 0.05) {
                    return "Your weight went up slightly. Review your calorie deficit and stay consistent.";
                }
                return "Your weight is holding steady. Small, consistent changes add up over time.";

            case "Weight Gain":
                if (diff > 0.05) {
                    return "Nice! You're gaining weight steadily toward your goal.";
                } else if (diff < -0.05) {
                    return "Your weight dropped a little. Make sure you're in a calorie surplus.";
                }
                return "Weight is steady. Consider slightly increasing your daily calorie intake.";

            case "Muscle Building":
                if (diff > 0.05) {
                    return "Good trend for muscle building - just make sure it's from training, not just fat gain.";
                } else if (diff < -0.05) {
                    return "You're losing weight - make sure you're eating enough protein and calories to build muscle.";
                }
                return "Stable weight. Keep progressively overloading your workouts.";

            case "Maintain Weight":
                if (Math.abs(diff) <= 0.5) {
                    return "You're maintaining well - great consistency!";
                }
                return "Your weight shifted a bit. Adjust your intake slightly to stay balanced.";

            case "General Fitness":
            default:
                return "Keep logging consistently - steady tracking is the key to long-term fitness.";
        }
    }
}
