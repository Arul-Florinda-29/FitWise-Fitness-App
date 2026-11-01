package model;

public class Workout {
    private String workoutName;
    private String goal;
    private String detail;

    public Workout() {
    }

    public Workout(String workoutName, String goal, String detail) {
        this.workoutName = workoutName;
        this.goal = goal;
        this.detail = detail;
    }

    public String getWorkoutName() { return workoutName; }
    public void setWorkoutName(String workoutName) { this.workoutName = workoutName; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
