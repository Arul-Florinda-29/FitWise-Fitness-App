package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Small online-learning model personalized per user. It learns the observed
 * reward for each strategy from subsequent real weekly outcomes (not a fixed
 * training dataset). Strategy values are exponential moving averages.
 */
public class ExperienceLearningModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, Double> workoutValues = new HashMap<>();
    private final Map<String, Integer> workoutCounts = new HashMap<>();
    private final Map<String, Double> dietValues = new HashMap<>();
    private final Map<String, Integer> dietCounts = new HashMap<>();

    public void learnWorkout(String strategy, double reward) { learn(workoutValues, workoutCounts, strategy, reward); }
    public void learnDiet(String strategy, double reward) { learn(dietValues, dietCounts, strategy, reward); }
    private void learn(Map<String,Double> values, Map<String,Integer> counts, String key, double reward) {
        if (key == null || key.trim().isEmpty()) return;
        reward = Math.max(0, Math.min(1, reward));
        int n = counts.getOrDefault(key, 0);
        double old = values.getOrDefault(key, 0.5);
        // More stable early learning, then gradually adapt to newer experience.
        double alpha = n < 3 ? 1.0 / (n + 1.0) : 0.25;
        values.put(key, old + alpha * (reward - old));
        counts.put(key, n + 1);
    }
    public double workoutScore(String strategy) { return workoutValues.getOrDefault(strategy, 0.5); }
    public double dietScore(String strategy) { return dietValues.getOrDefault(strategy, 0.5); }
    public int workoutSamples() { return workoutCounts.values().stream().mapToInt(Integer::intValue).sum(); }
    public int dietSamples() { return dietCounts.values().stream().mapToInt(Integer::intValue).sum(); }
    public int workoutCount(String strategy) { return workoutCounts.getOrDefault(strategy, 0); }
    public int dietCount(String strategy) { return dietCounts.getOrDefault(strategy, 0); }
    public String summary() { return "Online learning: " + workoutSamples() + " workout outcomes, " + dietSamples() + " diet outcomes learned from this user's saved weekly experience."; }
}
