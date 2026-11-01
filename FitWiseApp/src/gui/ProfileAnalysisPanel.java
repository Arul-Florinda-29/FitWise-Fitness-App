package gui;

import model.User;
import service.BMICalculator;
import service.CalorieCalculator;
import service.AdaptiveRecommendationEngine;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Page shown immediately after registration: BMI analysis and automatic goal. */
public class ProfileAnalysisPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JLabel bmi = Theme.title("—");
    private final JLabel category = Theme.header("—");
    private final JLabel goal = Theme.header("—");
    private final JLabel advice = Theme.bodyLabel("—");
    private final JLabel calories = Theme.bodyLabel("—");
    private final JLabel mlPlan = Theme.bodyLabel("—");

    public ProfileAnalysisPanel(MainApp a) {
        app=a; setOpaque(false); setLayout(new BorderLayout(18,18));
        setBorder(new EmptyBorder(30,45,25,45));
        add(Theme.title("YOUR PROFILE ANALYSIS"), BorderLayout.NORTH);

        JPanel center=Theme.transparentPanel(new GridLayout(1,2,18,18));
        JPanel bmiCard=Theme.cardPanel(new GridBagLayout());
        GridBagConstraints c=new GridBagConstraints(); c.gridx=0; c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1;
        c.gridy=0; c.insets=new Insets(10,15,6,15); bmiCard.add(Theme.header("BODY MASS INDEX"),c);
        c.gridy++; c.insets=new Insets(0,15,8,15); bmiCard.add(bmi,c);
        c.gridy++; c.insets=new Insets(0,15,18,15); bmiCard.add(category,c);
        c.gridy++; c.insets=new Insets(5,15,6,15); bmiCard.add(Theme.header("YOUR SELECTED FITNESS GOAL"),c);
        c.gridy++; c.insets=new Insets(0,15,10,15); bmiCard.add(goal,c);
        c.gridy++; c.insets=new Insets(0,15,10,15); bmiCard.add(advice,c);
        c.gridy++; c.insets=new Insets(0,15,10,15); bmiCard.add(calories,c);
        c.gridy++; c.insets=new Insets(8,15,6,15); bmiCard.add(Theme.header("WEEKLY EXPERIENCE ADAPTATION"),c);
        c.gridy++; c.insets=new Insets(0,15,10,15); bmiCard.add(mlPlan,c);

        JPanel how=Theme.cardPanel(new GridLayout(0,1,10,10));
        how.add(Theme.header("BMI INFORMATION (NOT USED TO CHOOSE YOUR GOAL)"));
        how.add(Theme.bodyLabel("BMI below 18.5 → Underweight"));
        how.add(Theme.bodyLabel("BMI 18.5–24.9 → Normal"));
        how.add(Theme.bodyLabel("BMI 25–29.9 → Overweight"));
        how.add(Theme.bodyLabel("BMI 30+ → Obese"));
        how.add(Theme.mutedLabel("Your food preference and allergy settings are then used to filter meals."));
        center.add(bmiCard); center.add(how); add(center,BorderLayout.CENTER);

        JPanel bottom=Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        JButton profile=Theme.secondaryButton("← EDIT PROFILE");
        JButton next=Theme.primaryButton("GO TO HOME →");
        profile.addActionListener(e->app.showPanel(MainApp.PROFILE));
        next.addActionListener(e->app.showPanel(MainApp.HOME));
        bottom.add(profile); bottom.add(next); add(bottom,BorderLayout.SOUTH);
    }

    public void refresh(){
        User u=app.getCurrentUser(); if(u==null)return;
        double b=BMICalculator.calculateBMI(u.getWeight(),u.getHeight());
        String cat=BMICalculator.getCategory(b);
        String auto=u.getFitnessGoal();
        bmi.setText(String.format("%.1f",b));
        category.setText(cat);
        category.setForeground(Theme.colorForBmiCategory(cat));
        goal.setText(auto);
        advice.setText("<html><div style='width:430px'>"+BMICalculator.getRecommendation(b)+"</div></html>");
        int kcal=CalorieCalculator.calculateDailyCalorieTarget(u.getWeight(),u.getHeight(),u.getAge(),u.getGender(),u.getActivityLevel(),auto);
        calories.setText("Estimated daily energy target: "+kcal+" kcal");
        String summary=new AdaptiveRecommendationEngine().getSummary(u);
        mlPlan.setText("<html><div style='width:430px'>"+summary.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>")+"</div></html>");
    }
}
