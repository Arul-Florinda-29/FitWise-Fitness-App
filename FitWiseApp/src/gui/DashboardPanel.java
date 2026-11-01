package gui;

import data.DataStore;
import model.Progress;
import model.User;
import service.BMICalculator;
import service.CalorieCalculator;
import service.SmartCoach;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.time.LocalDate;
import java.util.List;

/**
 * FitWise overview dashboard. The dashboard is the home screen after login,
 * while every feature remains available as its own page through the sidebar.
 */
public class DashboardPanel extends JPanel {
    private final MainApp app;
    private final JLabel hello = Theme.title("Hello");
    private final JLabel dailyQuote = Theme.bodyLabel("—");
    private final JLabel sub = Theme.subtitle("TODAY • YOUR PERSONALIZED PLAN");
    private final JLabel bmi = Theme.header("—"), category = Theme.mutedLabel("—");
    private final JLabel calories = Theme.header("—"), protein = Theme.header("—");
    private final JLabel water = Theme.header("—"), sleep = Theme.header("—");
    private final JLabel meals = Theme.header("—"), workout = Theme.header("—");
    private final JLabel goal = Theme.header("—"), coachText = Theme.bodyLabel("—");
    private final JProgressBar waterBar = new JProgressBar(), mealBar = new JProgressBar(), workoutBar = new JProgressBar();
    private final WeightChart chart = new WeightChart();

    public DashboardPanel(MainApp a) {
        app = a;
        setOpaque(false);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(22, 22, 18, 22));
        content.add(buildHeader(), BorderLayout.NORTH);
        content.add(buildCenter(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(new Color(5, 9, 11));
        side.setPreferredSize(new Dimension(220, 0));
        side.setBorder(new EmptyBorder(22, 16, 16, 12));

        JPanel top = new JPanel(); top.setOpaque(false); top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("FITWISE"); logo.setFont(new Font("SansSerif", Font.BOLD, 29)); logo.setForeground(Theme.ACCENT);
        JLabel tag = Theme.mutedLabel("ADAPTIVE FITNESS"); tag.setFont(new Font("SansSerif", Font.BOLD, 9));
        top.add(logo); top.add(Box.createVerticalStrut(2)); top.add(tag); top.add(Box.createVerticalStrut(25));

        top.add(sideButton("⌂  HOME", MainApp.HOME, true));
        top.add(sideButton("✦  SMART COACH", MainApp.COACH, false));
        top.add(sideButton("♢  WORKOUTS", MainApp.WORKOUT_PLAN, false));
        top.add(sideButton("▣  NUTRITION", MainApp.DIET_PLAN, false));
        top.add(sideButton("▤  PROGRESS", MainApp.PROGRESS, false));
        top.add(sideButton("▦  CALENDAR", MainApp.CALENDAR, false));
        top.add(sideButton("☾  SLEEP & RECOVERY", MainApp.SLEEP, false));
        top.add(sideButton("◇  CHALLENGES", MainApp.CHALLENGES, false));
        top.add(Box.createVerticalStrut(5));
        top.add(sideButton("≡  MORE TOOLS", MainApp.MORE, false));
        top.add(sideButton("⚙  SETTINGS", MainApp.SETTINGS, false));
        side.add(top, BorderLayout.NORTH);

        JButton logout = Theme.secondaryButton("↪  LOG OUT");
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.addActionListener(e -> app.logout());
        side.add(logout, BorderLayout.SOUTH);
        return side;
    }

    private JButton sideButton(String text, String target, boolean selected) {
        JButton b = selected ? Theme.primaryButton(text) : Theme.secondaryButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setPreferredSize(new Dimension(190, 42));
        b.setBorder(new EmptyBorder(10, 13, 10, 8));
        b.addActionListener(e -> app.showPanel(target));
        return b;
    }

    private JPanel buildHeader() {
        JPanel p = Theme.transparentPanel(new BorderLayout(12, 8));
        JPanel left = Theme.transparentPanel(new GridLayout(2, 1, 2, 2));
        left.add(hello); left.add(sub); p.add(left, BorderLayout.WEST);
        JPanel actions = Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton coach = Theme.primaryButton("✦ SMART COACH"); coach.addActionListener(e -> app.showPanel(MainApp.COACH));
        JButton alerts = Theme.secondaryButton("🔔 ALERTS"); alerts.addActionListener(e -> app.showPanel(MainApp.NOTIFICATIONS));
        JButton profile = Theme.secondaryButton("👤 PROFILE"); profile.addActionListener(e -> app.showPanel(MainApp.PROFILE));
        actions.add(coach); actions.add(alerts); actions.add(profile); p.add(actions, BorderLayout.EAST);
        return p;
    }

    private JPanel buildCenter() {
        JPanel root = new JPanel(new BorderLayout(14, 14)); root.setOpaque(false);

        JPanel topArea = new JPanel(new BorderLayout(10, 10)); topArea.setOpaque(false);
        JPanel quote = Theme.cardPanel(new BorderLayout(12, 6));
        quote.add(Theme.header("💬 DAILY MOTIVATION"), BorderLayout.WEST);
        dailyQuote.setHorizontalAlignment(SwingConstants.CENTER);
        dailyQuote.setFont(new Font("SansSerif", Font.BOLD, 15));
        quote.add(dailyQuote, BorderLayout.CENTER);

        JPanel metrics = new JPanel(new GridLayout(2, 4, 10, 10)); metrics.setOpaque(false);
        metrics.add(metric("BMI", bmi, category));
        metrics.add(metric("DAILY ENERGY", calories, Theme.mutedLabel("kcal target")));
        metrics.add(metric("PROTEIN", protein, Theme.mutedLabel("grams / day")));
        metrics.add(metric("WATER", water, waterBar));
        metrics.add(metric("SLEEP", sleep, Theme.mutedLabel("hours / target")));
        metrics.add(metric("MEALS", meals, mealBar));
        metrics.add(metric("WORKOUT", workout, workoutBar));
        metrics.add(metric("AUTOMATIC GOAL", goal, Theme.mutedLabel("based on BMI")));
        topArea.add(quote, BorderLayout.NORTH); topArea.add(metrics, BorderLayout.CENTER);
        root.add(topArea, BorderLayout.NORTH);

        JPanel middle = new JPanel(new GridLayout(1, 2, 14, 14)); middle.setOpaque(false);
        JPanel graphCard = Theme.cardPanel(new BorderLayout(8, 8));
        graphCard.add(Theme.header("WEIGHT & BMI PROGRESS"), BorderLayout.NORTH);
        graphCard.add(chart, BorderLayout.CENTER);
        JPanel graphButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); graphButtons.setOpaque(false);
        JButton progress = Theme.secondaryButton("OPEN PROGRESS →"); progress.addActionListener(e -> app.showPanel(MainApp.PROGRESS));
        JButton calendar = Theme.secondaryButton("MONTHLY CALENDAR →"); calendar.addActionListener(e -> app.showPanel(MainApp.CALENDAR));
        graphButtons.add(progress); graphButtons.add(calendar); graphCard.add(graphButtons, BorderLayout.SOUTH);

        JPanel insight = Theme.cardPanel(new BorderLayout(8, 8));
        insight.add(Theme.header("TODAY'S PLAN"), BorderLayout.NORTH);
        JPanel body = Theme.transparentPanel(new BorderLayout(8, 8));
        body.add(goal, BorderLayout.NORTH);
        body.add(coachText, BorderLayout.CENTER);
        JPanel nav = new JPanel(new GridLayout(2, 2, 8, 8)); nav.setOpaque(false);
        nav.add(action("🥗  MEAL PLAN", MainApp.DIET_PLAN));
        nav.add(action("🏋  WORKOUT PLAN", MainApp.WORKOUT_PLAN));
        nav.add(action("💧  WATER TRACKER", MainApp.WATER));
        nav.add(action("🌙  SLEEP TRACKER", MainApp.SLEEP));
        body.add(nav, BorderLayout.SOUTH); insight.add(body, BorderLayout.CENTER);

        middle.add(graphCard); middle.add(insight); root.add(middle, BorderLayout.CENTER);

        JPanel quick = Theme.cardPanel(new GridLayout(1, 6, 8, 8));
        quick.add(action("✦ SMART COACH", MainApp.COACH));
        quick.add(action("🏃 EXERCISES", MainApp.EXERCISES));
        quick.add(action("🍎 FOOD DATABASE", MainApp.NUTRITION));
        quick.add(action("📅 WEEKLY PLAN", MainApp.WEEKLY_PLAN));
        quick.add(action("🏆 CHALLENGES", MainApp.CHALLENGES));
        quick.add(action("⚙ SETTINGS", MainApp.SETTINGS));
        root.add(quick, BorderLayout.SOUTH);
        return root;
    }

    private JPanel metric(String title, JComponent value, JComponent bottom) {
        JPanel p = Theme.cardPanel(new BorderLayout(3, 4));
        p.add(Theme.mutedLabel(title), BorderLayout.NORTH); p.add(value, BorderLayout.CENTER); p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JButton action(String text, String target) {
        JButton b = Theme.secondaryButton(text); b.addActionListener(e -> app.showPanel(target)); return b;
    }

    public void refresh() {
        User u = app.getCurrentUser(); if (u == null) return;
        hello.setText("Hello, " + u.getName() + " 👋");
        double b = BMICalculator.calculateBMI(u.getWeight(), u.getHeight());
        String cat = BMICalculator.getCategory(b);
        bmi.setText(String.format("%.1f", b)); bmi.setForeground(Theme.colorForBmiCategory(cat)); category.setText(cat);
        String auto = u.getFitnessGoal(); goal.setText(auto);
        int kcal = CalorieCalculator.calculateDailyCalorieTarget(u.getWeight(), u.getHeight(), u.getAge(), u.getGender(), u.getActivityLevel(), auto);
        calories.setText(kcal + " kcal"); protein.setText(CalorieCalculator.calculateProteinTarget(u.getWeight(), auto) + " g");
        double wt = CalorieCalculator.calculateWaterIntakeLitres(u.getWeight()); double wc = DataStore.getWaterConsumed(u.getUserId()) / 1000.0;
        water.setText(String.format("%.1f / %.1f L", wc, wt)); configureBar(waterBar, wc, wt);
        double sl = DataStore.getSleep(u.getUserId()); sleep.setText(String.format("%.1f / %.1f h", sl, u.getSleepGoalHours()));
        int m = DataStore.getMealsEaten(u.getUserId(), LocalDate.now().toString()); meals.setText(m + " / 4"); configureBar(mealBar, m, 4);
        int w = DataStore.getWorkoutCompletedCount(u.getUserId(), LocalDate.now().toString()); workout.setText(w > 0 ? "Completed" : "Not done"); configureBar(workoutBar, Math.min(w, 1), 1);
        coachText.setText("<html><div style='width:390px'><b>Next best action</b><br><br>" + SmartCoach.getDailyInsight(u, b, DataStore.getSteps(u.getUserId()), DataStore.getWaterConsumed(u.getUserId())) + "</div></html>");
        dailyQuote.setText("<html><div style='width:520px'><i>&quot;" + dailyMotivation() + "&quot;</i></div></html>");
        chart.setData(DataStore.getProgressForUser(u.getUserId()), u);
    }


    private String dailyMotivation() {
        String[] quotes = {
            "Small steps every day lead to big results.",
            "Consistency beats perfection. Keep moving forward.",
            "Your future self will thank you for today's effort.",
            "Progress is progress, no matter how small.",
            "Take care of your body. It is the only place you have to live.",
            "You do not need to be perfect. You just need to be consistent.",
            "One healthy choice today can become a powerful habit tomorrow.",
            "Believe in your plan, trust the process, and keep going.",
            "Stronger every day—physically and mentally.",
            "Make today count. Your goals are worth the effort."
        };
        return quotes[(LocalDate.now().getDayOfYear() - 1) % quotes.length];
    }

    private void configureBar(JProgressBar bar, double value, double max) {
        bar.setMaximum(Math.max(1, (int)Math.round(max * 100))); bar.setValue(Math.min(bar.getMaximum(), (int)Math.round(value * 100)));
        bar.setStringPainted(true); bar.setString(String.format("%.0f%%", Math.min(100, value / Math.max(.01, max) * 100)));
    }

    /** Simple dependency-free chart using Java2D; uses the user's actual saved progress entries. */
    private static class WeightChart extends JPanel {
        private List<Progress> data = java.util.Collections.emptyList(); private User user;
        WeightChart() { setOpaque(false); setPreferredSize(new Dimension(500, 245)); }
        void setData(List<Progress> d, User u) { data = d == null ? java.util.Collections.emptyList() : d; user = u; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight(), l=48, r=18, t=22, b=36;
            g2.setColor(new Color(255,255,255,22));
            for(int i=0;i<4;i++){int y=t+i*(h-t-b)/3;g2.drawLine(l,y,w-r,y);}
            g2.setColor(Theme.MUTED_TEXT); g2.setFont(new Font("SansSerif",Font.PLAIN,10));
            if(data.isEmpty()) {g2.drawString("Log your weight to start the trend graph", l, h/2);g2.dispose();return;}
            double min=Double.MAX_VALUE,max=-Double.MAX_VALUE; for(Progress p:data){min=Math.min(min,p.getWeight());max=Math.max(max,p.getWeight());}
            if(user!=null){min=Math.min(min,user.getWeight());max=Math.max(max,user.getWeight());}
            double pad=Math.max(1,(max-min)*.2); min-=pad;max+=pad;
            g2.setColor(Theme.TEXT);g2.drawString(String.format("%.1f kg",max),4,t+4);g2.drawString(String.format("%.1f kg",min),4,h-b+4);
            int n=data.size(); int prevX=-1,prevY=-1;
            g2.setStroke(new BasicStroke(3f));g2.setColor(Theme.ACCENT);
            for(int i=0;i<n;i++){
                Progress p=data.get(i); int x=l+(n==1?0:(int)((w-l-r)*i/(double)(n-1))); int y=t+(int)((max-p.getWeight())/(max-min)*(h-t-b));
                if(prevX>=0)g2.drawLine(prevX,prevY,x,y); prevX=x;prevY=y;
                g2.fill(new Ellipse2D.Double(x-4,y-4,8,8));
                if(i==0||i==n-1){g2.setColor(Theme.MUTED_TEXT);g2.setFont(new Font("SansSerif",Font.PLAIN,9));g2.drawString(p.getDate(),Math.max(l,x-28),h-12);g2.setColor(Theme.ACCENT);}
            }
            g2.dispose();
        }
    }
}
