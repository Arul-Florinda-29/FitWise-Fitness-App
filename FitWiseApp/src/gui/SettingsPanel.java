package gui;

import data.DataStore;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JPanel list = new JPanel();
    private static final String[] KEYS = {"Water reminder", "Workout reminder", "Meal reminder", "Sleep reminder"};

    public SettingsPanel(MainApp a) {
        app = a;
        setOpaque(false);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(22, 28, 18, 28));
        add(Theme.title("ADVANCED SETTINGS"), BorderLayout.NORTH);
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        add(Theme.transparentScrollPane(list), BorderLayout.CENTER);
        JButton back = Theme.secondaryButton("\u2190 More");
        back.addActionListener(e -> a.showPanel(MainApp.MORE));
        add(back, BorderLayout.SOUTH);
    }

    // Rebuilding each time the panel is shown (instead of once at app startup, before
    // any user had logged in) ensures this always reflects whoever is currently signed in.
    public void setVisible(boolean v) {
        super.setVisible(v);
        if (v) load();
    }

    private void load() {
        list.removeAll();
        User u = app.getCurrentUser();
        for (String key : KEYS) {
            JPanel p = Theme.cardPanel(new BorderLayout());
            JCheckBox c = new JCheckBox(key);
            c.setOpaque(false);
            c.setForeground(Theme.TEXT);
            c.setFont(Theme.FONT_HEADER);
            c.setSelected(u != null && DataStore.getNotificationSettings(u.getUserId()).getOrDefault(key, true));
            c.addActionListener(e -> {
                User current = app.getCurrentUser();
                if (current != null) DataStore.setNotification(current.getUserId(), key, c.isSelected());
            });
            p.add(c, BorderLayout.CENTER);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
            list.add(p);
            list.add(Box.createVerticalStrut(9));
        }
        JPanel modes = Theme.cardPanel(new GridLayout(0, 2, 10, 10));
        modes.add(Theme.header("Personalization engine"));
        modes.add(Theme.bodyLabel("BMI + BMR + TDEE + goal + activity + food preference + allergies"));
        modes.add(Theme.header("Data storage"));
        modes.add(Theme.bodyLabel("Saved locally on this computer \u2022 your account persists between sessions"));
        modes.add(Theme.header("Plan refresh"));
        modes.add(Theme.bodyLabel("Automatic after profile changes"));
        list.add(modes);
        list.revalidate();
        list.repaint();
    }
}
