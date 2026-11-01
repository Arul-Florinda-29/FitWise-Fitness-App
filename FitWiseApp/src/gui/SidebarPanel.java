package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Reusable navigation sidebar shown on every logged-in feature page. */
public class SidebarPanel extends JPanel {
    private final MainApp app;

    public SidebarPanel(MainApp a) {
        app = a;
        setLayout(new BorderLayout());
        setBackground(new Color(5, 9, 11));
        setPreferredSize(new Dimension(220, 0));
        setBorder(new EmptyBorder(22, 16, 16, 12));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("FITWISE");
        logo.setFont(new Font("SansSerif", Font.BOLD, 29));
        logo.setForeground(Theme.ACCENT);
        JLabel tag = Theme.mutedLabel("ADAPTIVE FITNESS");
        tag.setFont(new Font("SansSerif", Font.BOLD, 9));
        top.add(logo);
        top.add(Box.createVerticalStrut(2));
        top.add(tag);
        top.add(Box.createVerticalStrut(22));

        addButton(top, "⌂  HOME", MainApp.HOME);
        addButton(top, "✦  SMART COACH", MainApp.COACH);
        addButton(top, "♢  WORKOUT PLAN", MainApp.WORKOUT_PLAN);
        addButton(top, "▣  DIET PLANNER", MainApp.DIET_PLAN);
        addButton(top, "💧  WATER TRACKER", MainApp.WATER);
        addButton(top, "☾  SLEEP & RECOVERY", MainApp.SLEEP);
        addButton(top, "▤  PROGRESS", MainApp.PROGRESS);
        addButton(top, "▦  MONTHLY CALENDAR", MainApp.CALENDAR);
        addButton(top, "📅  WEEKLY PLAN", MainApp.WEEKLY_PLAN);
        addButton(top, "🏃  EXERCISE LIBRARY", MainApp.EXERCISES);
        addButton(top, "🍎  FOOD DATABASE", MainApp.NUTRITION);
        addButton(top, "🏆  CHALLENGES", MainApp.CHALLENGES);
        addButton(top, "🔔  NOTIFICATIONS", MainApp.NOTIFICATIONS);
        addButton(top, "👤  PROFILE", MainApp.PROFILE);
        addButton(top, "⚙  SETTINGS", MainApp.SETTINGS);
        addButton(top, "≡  MORE TOOLS", MainApp.MORE);

        add(new JScrollPane(top) {{
            setBorder(null);
            setOpaque(false);
            getViewport().setOpaque(false);
            setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            getVerticalScrollBar().setUnitIncrement(12);
        }}, BorderLayout.CENTER);

        JButton logout = Theme.secondaryButton("↪  LOG OUT");
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.addActionListener(e -> app.logout());
        add(logout, BorderLayout.SOUTH);
    }

    private void addButton(JPanel top, String text, String target) {
        JButton b = Theme.secondaryButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setPreferredSize(new Dimension(190, 40));
        b.setBorder(new EmptyBorder(9, 10, 9, 6));
        b.addActionListener(e -> app.showPanel(target));
        top.add(b);
        top.add(Box.createVerticalStrut(4));
    }
}
