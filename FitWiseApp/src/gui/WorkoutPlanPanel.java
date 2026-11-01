package gui;

import data.DataStore;
import data.BackendClient;
import model.*;
import service.WorkoutRecommendation;
import service.AdaptiveRecommendationEngine;
import model.WeeklyAdaptation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JPanel list = new JPanel();
    private final WorkoutRecommendation svc = new WorkoutRecommendation();
    private final JLabel todayStatus = Theme.header("");
    private final List<JCheckBox> checkboxes = new ArrayList<>();
    private List<Workout> currentPlan = new ArrayList<>();

    public WorkoutPlanPanel(MainApp a) {
        app = a;
        setOpaque(false);
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(22, 28, 18, 28));

        JPanel header = Theme.darkPanel(new BorderLayout());
        header.add(Theme.title("ADAPTIVE WORKOUT ENGINE \uD83C\uDFCB"), BorderLayout.NORTH);
        JPanel statusRow = Theme.transparentPanel(new BorderLayout());
        statusRow.add(todayStatus, BorderLayout.WEST);
        JPanel actions = Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton markAll = Theme.primaryButton("\u2713 SUBMIT: TODAY'S WORKOUT DONE");
        JButton resetToday = Theme.secondaryButton("RESET TODAY");
        JButton weeklyReview = Theme.primaryButton("WEEKLY EXPERIENCE REVIEW");
        markAll.addActionListener(e -> markAllDone());
        resetToday.addActionListener(e -> resetToday());
        weeklyReview.addActionListener(e -> weeklyReview());
        actions.add(weeklyReview);
        actions.add(markAll);
        actions.add(resetToday);
        statusRow.add(actions, BorderLayout.EAST);
        header.add(statusRow, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        add(Theme.transparentScrollPane(list), BorderLayout.CENTER);

        JButton home = Theme.primaryButton("⌂ HOME");
        home.addActionListener(e -> app.showPanel(MainApp.HOME));
        JPanel nav = Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT));
        nav.add(home);
        add(nav, BorderLayout.SOUTH);
    }

    public void setVisible(boolean v) {
        super.setVisible(v);
        if (v) load();
    }

    public void refresh() { load(); }

    private void load() {
        list.removeAll();
        checkboxes.clear();
        User u = app.getCurrentUser();
        if (u == null) return;

        currentPlan = svc.getWorkoutPlan(u);
        list.add(Theme.subtitle("Style: " + u.getWorkoutPreference() + "  \u2022  Goal: " + u.getFitnessGoal()));
        WeeklyAdaptation saved = new AdaptiveRecommendationEngine().getCurrent(u);
        JPanel reviewCard = Theme.cardPanel(new BorderLayout(6,6));
        reviewCard.add(Theme.header("WEEKLY EXPERIENCE ADAPTATION"), BorderLayout.NORTH);
        reviewCard.add(Theme.bodyLabel(new AdaptiveRecommendationEngine().getSummary(u).replace("\n","<br>")), BorderLayout.CENTER);
        list.add(reviewCard); list.add(Box.createVerticalStrut(8));

        for (Workout w : currentPlan) {
            JPanel c = Theme.cardPanel(new BorderLayout(10, 5));
            JPanel l = Theme.darkPanel(new GridLayout(2, 1));
            l.add(Theme.header(w.getWorkoutName()));
            l.add(Theme.mutedLabel("Adaptive recommendation \u2022 " + w.getGoal()));
            c.add(l, BorderLayout.CENTER);

            JCheckBox d = new JCheckBox(w.getDetail() + "   \u2713 Done");
            d.setOpaque(false);
            d.setForeground(Theme.TEXT);
            d.setSelected(DataStore.isWorkoutCompleted(u.getUserId(), w.getWorkoutName()));
            d.addActionListener(e -> {
                DataStore.setWorkoutCompleted(u.getUserId(), w.getWorkoutName(), d.isSelected());
                syncTodayStatus(u);
            });
            checkboxes.add(d);
            c.add(d, BorderLayout.EAST);
            list.add(c);
            list.add(Box.createVerticalStrut(8));
        }
        list.revalidate();
        list.repaint();
        syncTodayStatus(u);
    }

    /** Recomputes whether "today" should show as completed on the monthly calendar. */
    private void syncTodayStatus(User u) {
        boolean anyDone = false;
        for (Workout w : currentPlan) {
            if (DataStore.isWorkoutCompleted(u.getUserId(), w.getWorkoutName())) { anyDone = true; break; }
        }
        DataStore.setWorkoutDayStatus(u.getUserId(), LocalDate.now().toString(), anyDone);
        todayStatus.setText(anyDone ? "\u2705 Today's workout is marked complete" : "\u23F3 Today's workout is not logged yet");
    }

    private void weeklyReview() {
        User u=app.getCurrentUser(); if(u==null)return;
        JComboBox<String> difficulty=new JComboBox<>(new String[]{"Moderate","Easy","Hard"});
        JComboBox<String> energy=new JComboBox<>(new String[]{"Okay","High","Low"});
        JComboBox<String> hunger=new JComboBox<>(new String[]{"Okay","Low","High"});
        JComboBox<String> meals=new JComboBox<>(new String[]{"Okay","Good","Poor"});
        JComboBox<String> discomfort=new JComboBox<>(new String[]{"No","Yes"});
        JPanel form=new JPanel(new GridLayout(0,2,8,8));
        form.add(new JLabel("Workout difficulty this week"));form.add(difficulty);
        form.add(new JLabel("Energy level"));form.add(energy);
        form.add(new JLabel("Hunger between meals"));form.add(hunger);
        form.add(new JLabel("Meal plan satisfaction"));form.add(meals);
        form.add(new JLabel("Pain/discomfort during exercise?"));form.add(discomfort);
        int result=JOptionPane.showConfirmDialog(this,form,"Weekly experience review",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(result!=JOptionPane.OK_OPTION)return;
        String d=(String)difficulty.getSelectedItem(), en=(String)energy.getSelectedItem(), hu=(String)hunger.getSelectedItem(), ms=(String)meals.getSelectedItem(), dc=(String)discomfort.getSelectedItem();
        WeeklyAdaptation a=new AdaptiveRecommendationEngine().reviewAndSave(u,d,en,hu,ms,dc);
        String backendNote="\nBackend sync: saved locally.";
        try {
            BackendClient.saveExperience("weekly_review", "workout="+a.getWorkoutStrategy()+";diet="+a.getDietStrategy()+";difficulty="+d+";energy="+en+";hunger="+hu+";mealSatisfaction="+ms+";discomfort="+dc+";workoutAdherence="+a.getWorkoutAdherence()+";mealAdherence="+a.getMealAdherence()+";weightChangeKg="+a.getWeightChangeKg()+";calorieAdjustment="+a.getCalorieAdjustment(), a.getReviewDate(), a.getExplanation());
            backendNote="\nBackend sync: weekly experience saved to backend.";
        } catch(Exception syncError) { backendNote="\nBackend sync unavailable; your review is still saved locally."; }
        load();
        JOptionPane.showMessageDialog(this,"Next plan adaptation saved.\n\n"+a.getExplanation()+"\n\nWorkout: "+AdaptiveRecommendationEngine.displayWorkout(a.getWorkoutStrategy())+"\nDiet: "+AdaptiveRecommendationEngine.displayDiet(a.getDietStrategy())+backendNote,"Weekly review saved",JOptionPane.INFORMATION_MESSAGE);
    }

    private void markAllDone() {
        User u = app.getCurrentUser();
        if (u == null) return;
        for (JCheckBox cb : checkboxes) cb.setSelected(true);
        for (Workout w : currentPlan) DataStore.setWorkoutCompleted(u.getUserId(), w.getWorkoutName(), true);
        syncTodayStatus(u);
        JOptionPane.showMessageDialog(this, "Nice work! Today's workout has been submitted and will show on your monthly calendar.");
    }

    private void resetToday() {
        User u = app.getCurrentUser();
        if (u == null) return;
        for (JCheckBox cb : checkboxes) cb.setSelected(false);
        for (Workout w : currentPlan) DataStore.setWorkoutCompleted(u.getUserId(), w.getWorkoutName(), false);
        syncTodayStatus(u);
    }
}
