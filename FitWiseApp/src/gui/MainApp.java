package gui;

import data.DataStore;
import model.User;
import service.BMICalculator;
import javax.swing.*;
import java.awt.*;

/** FitWise application with a reusable sidebar on every logged-in page. */
public class MainApp extends JFrame {
    public static final String WELCOME="WELCOME", REGISTER="REGISTER", LOGIN="LOGIN", ANALYSIS="ANALYSIS",
            DIET_PLAN="DIET_PLAN", WORKOUT_PLAN="WORKOUT_PLAN", WATER="WATER", SLEEP="SLEEP",
            PROGRESS="PROGRESS", CALENDAR="CALENDAR", PROFILE="PROFILE";
    // Kept for compatibility with older panels; it is not displayed as a dashboard.
    public static final String HOME="HOME", DASHBOARD=HOME, NUTRITION="NUTRITION", WEEKLY_PLAN="WEEKLY_PLAN", EXERCISES="EXERCISES", NOTIFICATIONS="NOTIFICATIONS", SETTINGS="SETTINGS", COACH="COACH", CHALLENGES="CHALLENGES", MORE="MORE";

    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private User currentUser;

    public MainApp() {
        setTitle("FitWise | Personalized Fitness Planner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 760);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);

        pages.setOpaque(false);
        pages.add(new WelcomePanel(this), WELCOME);
        pages.add(new DashboardPanel(this), HOME);
        pages.add(new RegisterPanel(this), REGISTER);
        pages.add(new LoginPanel(this), LOGIN);
        addPage(new ProfileAnalysisPanel(this), ANALYSIS);
        addPage(new DietPlanPanel(this), DIET_PLAN);
        addPage(new WorkoutPlanPanel(this), WORKOUT_PLAN);
        addPage(new WaterTrackerPanel(this), WATER);
        addPage(new SleepRecoveryPanel(this), SLEEP);
        addPage(new ProgressPanel(this), PROGRESS);
        addPage(new CalendarPanel(this), CALENDAR);
        addPage(new SmartCoachPanel(this), COACH);
        addPage(new NotificationsPanel(this), NOTIFICATIONS);
        addPage(new ProfilePanel(this), PROFILE);
        addPage(new ExerciseLibraryPanel(this), EXERCISES);
        addPage(new NutritionPanel(this), NUTRITION);
        addPage(new WeeklyPlanPanel(this), WEEKLY_PLAN);
        addPage(new ChallengesPanel(this), CHALLENGES);
        addPage(new SettingsPanel(this), SETTINGS);
        addPage(new MorePanel(this), MORE);

        Theme.BackgroundPanel root = new Theme.BackgroundPanel("assets/fitness_bg.png");
        root.setLayout(new BorderLayout());
        root.add(pages, BorderLayout.CENTER);
        setContentPane(root);
        showPanel(WELCOME);
    }

    private void addPage(Component page, String name) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(new SidebarPanel(this), BorderLayout.WEST);
        wrapper.add(page, BorderLayout.CENTER);
        pages.add(wrapper, name);
    }

    public void showPanel(String name) {
        if (currentUser == null && !(WELCOME.equals(name) || LOGIN.equals(name) || REGISTER.equals(name))) {
            cards.show(pages, LOGIN);
            return;
        }
        if (HOME.equals(name) || DASHBOARD.equals(name)) ((DashboardPanel) find(HOME)).refresh();
        if (ANALYSIS.equals(name)) ((ProfileAnalysisPanel) find(ANALYSIS)).refresh();
        if (WATER.equals(name)) ((WaterTrackerPanel) find(WATER)).refresh();
        if (SLEEP.equals(name)) ((SleepRecoveryPanel) find(SLEEP)).refresh();
        if (PROGRESS.equals(name)) ((ProgressPanel) find(PROGRESS)).refresh();
        if (CALENDAR.equals(name)) ((CalendarPanel) find(CALENDAR)).refresh();
        if (WORKOUT_PLAN.equals(name)) ((WorkoutPlanPanel) find(WORKOUT_PLAN)).refresh();
        if (DIET_PLAN.equals(name)) ((DietPlanPanel) find(DIET_PLAN)).refresh();
        if (COACH.equals(name)) ((SmartCoachPanel) find(COACH)).refresh();
        if (CHALLENGES.equals(name)) ((ChallengesPanel) find(CHALLENGES)).refresh();
        if (EXERCISES.equals(name)) ((ExerciseLibraryPanel) find(EXERCISES)).refresh();
        if (WEEKLY_PLAN.equals(name)) ((WeeklyPlanPanel) find(WEEKLY_PLAN)).refresh();
        cards.show(pages, name);
        pages.revalidate(); pages.repaint();
    }

    private Component find(String name) {
        for (Component c : pages.getComponents()) {
            Component found = findInTree(c, name);
            if (found != null) return found;
        }
        return null;
    }

    private Component findInTree(Component c, String name) {
        if (c.getName() != null && c.getName().equals(name)) return c;
        if (name.equals(ANALYSIS) && c instanceof ProfileAnalysisPanel) return c;
        if (name.equals(WATER) && c instanceof WaterTrackerPanel) return c;
        if (name.equals(SLEEP) && c instanceof SleepRecoveryPanel) return c;
        if (name.equals(PROGRESS) && c instanceof ProgressPanel) return c;
        if (name.equals(CALENDAR) && c instanceof CalendarPanel) return c;
        if (name.equals(WORKOUT_PLAN) && c instanceof WorkoutPlanPanel) return c;
        if (name.equals(DIET_PLAN) && c instanceof DietPlanPanel) return c;
        if (name.equals(COACH) && c instanceof SmartCoachPanel) return c;
        if (name.equals(CHALLENGES) && c instanceof ChallengesPanel) return c;
        if (name.equals(EXERCISES) && c instanceof ExerciseLibraryPanel) return c;
        if (name.equals(WEEKLY_PLAN) && c instanceof WeeklyPlanPanel) return c;
        if ((name.equals(HOME) || name.equals(DASHBOARD)) && c instanceof DashboardPanel) return c;
        if (c instanceof Container) {
            for (Component child : ((Container)c).getComponents()) {
                Component found = findInTree(child, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User u) {
        currentUser = u;
        // Preserve the goal explicitly selected by the user. BMI is a health
        // indicator and must not silently overwrite goals such as muscle gain
        // or weight maintenance when the user signs in.
        if (u != null) {
            if (u.getFitnessGoal() == null || u.getFitnessGoal().trim().isEmpty()) {
                double bmi = BMICalculator.calculateBMI(u.getWeight(), u.getHeight());
                u.setFitnessGoal(BMICalculator.getAutomaticGoal(bmi));
                DataStore.saveUserChanges();
            }
        }
    }

    public void logout() {
        currentUser = null;
        showPanel(WELCOME);
    }

    public static void main(String[] args) {
        // Use a predictable cross-platform Swing look-and-feel so native Windows
        // text-field metrics do not clip text on high-DPI displays.
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}
