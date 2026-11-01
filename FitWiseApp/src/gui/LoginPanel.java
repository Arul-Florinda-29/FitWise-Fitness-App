package gui;

import data.DataStore;
import data.BackendClient;
import java.util.LinkedHashMap;
import java.util.Map;
import model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends Theme.BackgroundPanel {
    public LoginPanel(MainApp app) {
        super("assets/fitness_bg.png");
        setLayout(new BorderLayout(22, 22));
        setBorder(new EmptyBorder(28, 44, 24, 44));

        JPanel top = Theme.transparentPanel(new BorderLayout());
        JLabel logo = new JLabel("FITWISE");
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setForeground(Theme.ACCENT);
        JLabel status = new JLabel("MEMBER ACCESS  •  PERSONALIZED FITNESS INTELLIGENCE");
        status.setFont(new Font("SansSerif", Font.BOLD, 10));
        status.setForeground(Theme.MUTED_TEXT);
        top.add(logo, BorderLayout.WEST);
        top.add(status, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel body = Theme.transparentPanel(new GridLayout(1, 2, 22, 0));

        JPanel hero = Theme.cardPanel(new BorderLayout(14, 14));
        hero.setBorder(new EmptyBorder(16, 16, 16, 16));
        hero.add(new Theme.FitnessImagePanel("assets/fitwise_reference.png"), BorderLayout.CENTER);
        JPanel heroText = Theme.transparentPanel(new BorderLayout(5, 5));
        JLabel h = new JLabel("TRAIN SMARTER. LIVE STRONGER.");
        h.setFont(new Font("SansSerif", Font.BOLD, 20));
        h.setForeground(Theme.TEXT);
        heroText.add(h, BorderLayout.NORTH);
        heroText.add(Theme.subtitle("Adaptive training • personalized nutrition • recovery tracking"), BorderLayout.SOUTH);
        hero.add(heroText, BorderLayout.SOUTH);

        JPanel card = Theme.cardPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(50, 58, 42, 58));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;

        JLabel over = new JLabel("WELCOME BACK  /  MEMBER LOGIN");
        over.setFont(new Font("SansSerif", Font.BOLD, 10)); over.setForeground(Theme.ACCENT);
        c.gridy=0;c.insets=new Insets(0,0,10,0);card.add(over,c);

        JLabel title = new JLabel("SIGN IN TO FITWISE");
        title.setFont(new Font("SansSerif", Font.BOLD, 34)); title.setForeground(Theme.TEXT);
        c.gridy++;c.insets=new Insets(0,0,5,0);card.add(title,c);

        JLabel sub = Theme.subtitle("Continue your personalized fitness journey.");
        c.gridy++;c.insets=new Insets(0,0,28,0);card.add(sub,c);

        JTextField email = new JTextField();
        JPasswordField pass = new JPasswordField();
        Theme.styleTextField(email); Theme.styleTextField(pass);
        email.setPreferredSize(new Dimension(380, 50)); pass.setPreferredSize(new Dimension(380, 50));
        addField(card,c,"EMAIL ADDRESS *",email);
        addField(card,c,"PASSWORD *",pass);

        JButton in = Theme.primaryButton("SIGN IN TO MY PROFILE  →");
        in.setPreferredSize(new Dimension(380, 52));
        c.gridy++; c.insets=new Insets(4,0,10,0); card.add(in,c);

        JButton create = Theme.secondaryButton("NEW TO FITWISE?  CREATE MY PROFILE");
        create.setPreferredSize(new Dimension(380, 46));
        c.gridy++; c.insets=new Insets(0,0,22,0); card.add(create,c);

        JLabel hint = new JLabel("ENTER  →  NEXT FIELD     •     ENTER ON PASSWORD  →  SIGN IN");
        hint.setFont(new Font("SansSerif",Font.BOLD,9)); hint.setForeground(Theme.MUTED_TEXT); hint.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy++;c.insets=new Insets(0,0,7,0);card.add(hint,c);
        JLabel privacy = new JLabel("Local demo data • Your recommendations are calculated from your profile");
        privacy.setFont(new Font("SansSerif",Font.PLAIN,9));privacy.setForeground(Theme.MUTED_TEXT);privacy.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy++;card.add(privacy,c);

        body.add(hero); body.add(card); add(body, BorderLayout.CENTER);

        JButton back = Theme.secondaryButton("← BACK TO FITWISE");
        back.addActionListener(e -> app.showPanel(MainApp.WELCOME));
        add(back, BorderLayout.SOUTH);

        bindEnter(email, () -> pass.requestFocusInWindow());
        bindEnter(pass, in::doClick);
        in.addActionListener(e -> {
            String em=email.getText().trim(), pw=new String(pass.getPassword());
            User u = DataStore.findByCredentials(em, pw);
            try {
                if (!BackendClient.isAvailable()) throw new IllegalStateException("FitWise backend is not running. Start it using backend\\run-backend.bat, then try again.");
                try {
                    BackendClient.login(em,pw);
                } catch (Exception backendLoginError) {
                    // Migrate an existing local account to the backend on first successful local login.
                    if (u == null) throw backendLoginError;
                    Map<String,String> profile=new LinkedHashMap<>();
                    profile.put("name",u.getName()); profile.put("email",u.getEmail()); profile.put("password",pw);
                    profile.put("age",String.valueOf(u.getAge())); profile.put("height",String.valueOf(u.getHeight()));
                    profile.put("weight",String.valueOf(u.getWeight())); profile.put("gender",u.getGender());
                    profile.put("activity",u.getActivityLevel()); profile.put("goal",u.getFitnessGoal());
                    profile.put("diet",u.getFoodPreference()); profile.put("allergies",u.getAllergies());
                    profile.put("targetWeight",String.valueOf(u.getTargetWeight())); profile.put("workout",u.getWorkoutPreference());
                    profile.put("units",u.getUnits()); profile.put("stepsGoal",String.valueOf(u.getDailyStepGoal()));
                    profile.put("sleepGoal",String.valueOf(u.getSleepGoalHours()));
                    BackendClient.register(profile);
                    BackendClient.login(em,pw);
                }
                Map<String,String> saved = BackendClient.getProfile();
                // Match by email after backend authentication so a changed password or a
                // profile created on another installation does not create duplicate local users.
                if (u == null) u = DataStore.findByEmail(em);
                if (u == null) {
                    String savedName = saved.getOrDefault("name", em);
                    int savedAge = parseInt(saved.get("age"), 25);
                    double savedHeight = parseDouble(saved.get("height"), 170);
                    double savedWeight = parseDouble(saved.get("weight"), 70);
                    u = new User(savedName, em, pw, savedAge,
                            saved.getOrDefault("gender", "Other"), savedHeight, savedWeight,
                            parseDouble(saved.get("targetWeight"), savedWeight),
                            saved.getOrDefault("activity", "Moderately Active"),
                            saved.getOrDefault("goal", "General Fitness"),
                            saved.getOrDefault("diet", "Vegetarian"),
                            saved.getOrDefault("workout", "Full Body"),
                            saved.getOrDefault("allergies", "None"),
                            saved.getOrDefault("units", "Metric"),
                            parseInt(saved.get("stepsGoal"), 8000),
                            parseDouble(saved.get("sleepGoal"), 8));
                    DataStore.addUser(u);
                } else {
                    // Refresh profile fields from the backend without replacing the local user ID,
                    // which keeps locally saved progress/tracker history associated with this account.
                    u.setName(saved.getOrDefault("name", u.getName()));
                    u.setPassword(pw);
                    u.setAge(parseInt(saved.get("age"), u.getAge()));
                    u.setGender(saved.getOrDefault("gender", u.getGender()));
                    u.setHeight(parseDouble(saved.get("height"), u.getHeight()));
                    u.setWeight(parseDouble(saved.get("weight"), u.getWeight()));
                    u.setTargetWeight(parseDouble(saved.get("targetWeight"), u.getTargetWeight()));
                    u.setActivityLevel(saved.getOrDefault("activity", u.getActivityLevel()));
                    u.setFitnessGoal(saved.getOrDefault("goal", u.getFitnessGoal()));
                    u.setFoodPreference(saved.getOrDefault("diet", u.getFoodPreference()));
                    u.setWorkoutPreference(saved.getOrDefault("workout", u.getWorkoutPreference()));
                    u.setAllergies(saved.getOrDefault("allergies", u.getAllergies()));
                    u.setUnits(saved.getOrDefault("units", u.getUnits()));
                    u.setDailyStepGoal(parseInt(saved.get("stepsGoal"), u.getDailyStepGoal()));
                    u.setSleepGoalHours(parseDouble(saved.get("sleepGoal"), u.getSleepGoalHours()));
                    DataStore.saveUserChanges();
                }
                app.setCurrentUser(u); app.showPanel(MainApp.HOME);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,ex.getMessage()==null?"Login failed.":ex.getMessage(),"Login failed",JOptionPane.WARNING_MESSAGE);
                pass.requestFocusInWindow();
            }
        });
        create.addActionListener(e -> app.showPanel(MainApp.REGISTER));
        SwingUtilities.invokeLater(() -> email.requestFocusInWindow());
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (Exception e) { return fallback; }
    }
    private double parseDouble(String value, double fallback) {
        try { return Double.parseDouble(value); } catch (Exception e) { return fallback; }
    }

    private void addField(JPanel p, GridBagConstraints c, String label, JComponent field) {
        c.gridy++; c.insets=new Insets(0,0,6,0);
        JLabel l=new JLabel(label);l.setFont(new Font("SansSerif",Font.BOLD,11));l.setForeground(Theme.TEXT);p.add(l,c);
        c.gridy++;c.insets=new Insets(0,0,16,0);p.add(field,c);
    }
    private void bindEnter(JComponent x, Runnable r) {
        String key="fitwise-enter"; x.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"),key);
        x.getActionMap().put(key,new AbstractAction(){public void actionPerformed(java.awt.event.ActionEvent e){r.run();}});
    }
}
