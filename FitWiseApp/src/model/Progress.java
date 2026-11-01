package model;

public class Progress implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private int progressId;
    private int userId;
    private String date;
    private double weight;
    private double bmi;

    public Progress() {
    }

    public Progress(int userId, String date, double weight, double bmi) {
        this.userId = userId;
        this.date = date;
        this.weight = weight;
        this.bmi = bmi;
    }

    public int getProgressId() { return progressId; }
    public void setProgressId(int progressId) { this.progressId = progressId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }
}
