package gui;

import data.DataStore;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NotificationsPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JPanel list = new JPanel();
    private static final String[] KEYS = {"Water reminder", "Workout reminder", "Meal reminder", "Sleep reminder"};

    public NotificationsPanel(MainApp a) {
        app = a;
        setOpaque(false);
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(22, 28, 18, 28));
        add(Theme.title("REMINDERS & SMART ALERTS \uD83D\uDD14"), BorderLayout.NORTH);
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        add(Theme.transparentScrollPane(list), BorderLayout.CENTER);
        JButton back = Theme.secondaryButton("\u2190 Home");
        back.addActionListener(e -> a.showPanel(MainApp.DASHBOARD));
        add(back, BorderLayout.SOUTH);
    }

    // Rebuilding on every show (rather than once at construction time, before anyone
    // had logged in) is what makes each toggle reflect the currently signed-in user.
    public void setVisible(boolean v) {
        super.setVisible(v);
        if (v) load();
    }

    private void load() {
        list.removeAll();
        User u = app.getCurrentUser();
        for (String key : KEYS) {
            JPanel c = Theme.cardPanel(new BorderLayout());
            JCheckBox b = new JCheckBox(key);
            b.setOpaque(false);
            b.setForeground(Theme.TEXT);
            b.setFont(Theme.FONT_HEADER);
            b.setSelected(u != null && DataStore.getNotificationSettings(u.getUserId()).getOrDefault(key, true));
            b.addActionListener(e -> {
                User current = app.getCurrentUser();
                if (current != null) DataStore.setNotification(current.getUserId(), key, b.isSelected());
            });
            c.add(b, BorderLayout.CENTER);
            c.add(Theme.mutedLabel("Adaptive notification \u2022 can be changed anytime"), BorderLayout.SOUTH);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
            list.add(c);
            list.add(Box.createVerticalStrut(9));
        }
        list.revalidate();
        list.repaint();
    }
}
