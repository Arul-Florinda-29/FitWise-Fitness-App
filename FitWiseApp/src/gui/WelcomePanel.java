package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WelcomePanel extends Theme.BackgroundPanel {
    public WelcomePanel(MainApp app) {
        super("assets/fitness_bg.png");
        setLayout(new BorderLayout(18, 18));
        setBorder(new EmptyBorder(24, 38, 20, 38));

        JPanel header = Theme.transparentPanel(new BorderLayout());
        JLabel logo = new JLabel("FITWISE");
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setForeground(Theme.ACCENT);
        JLabel platform = new JLabel("●  ADAPTIVE FITNESS INTELLIGENCE  /  v2.0");
        platform.setFont(new Font("SansSerif", Font.BOLD, 10));
        platform.setForeground(Theme.MUTED_TEXT);
        header.add(logo, BorderLayout.WEST);
        header.add(platform, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel content = Theme.transparentPanel(new GridLayout(1, 2, 22, 0));

        JPanel left = Theme.cardPanel(new BorderLayout(14, 14));
        left.setBorder(new EmptyBorder(38, 42, 30, 42));

        JLabel eyebrow = new JLabel("PERSONALIZED FITNESS • AI-STYLE RECOMMENDATION ENGINE");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 10));
        eyebrow.setForeground(Theme.ACCENT);

        JLabel title = new JLabel("<html>YOUR BODY.<br>YOUR DATA.<br><span style='color:#BEFF00'>YOUR PLAN.</span></html>");
        title.setFont(new Font("SansSerif", Font.BOLD, 48));
        title.setForeground(Theme.TEXT);

        JLabel desc = new JLabel("<html><div style='width:590px'>FitWise combines BMI, BMR, TDEE, activity, goals, food preferences and recovery habits to build a plan that adapts with you.</div></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        desc.setForeground(Theme.MUTED_TEXT);

        JPanel top = Theme.transparentPanel(new BorderLayout(8, 10));
        top.add(eyebrow, BorderLayout.NORTH);
        top.add(title, BorderLayout.CENTER);
        top.add(desc, BorderLayout.SOUTH);

        JPanel featureGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        featureGrid.setOpaque(false);
        featureGrid.add(feature("BODY ANALYTICS", "BMI • BMR • TDEE"));
        featureGrid.add(feature("SMART COACH", "Daily next-best action"));
        featureGrid.add(feature("MEAL ENGINE", "Goal-based nutrition"));
        featureGrid.add(feature("WORKOUT ENGINE", "Adaptive training"));
        featureGrid.add(feature("RECOVERY", "Sleep • water • steps"));
        featureGrid.add(feature("PROGRESS LAB", "Weight • BMI • consistency"));

        JPanel actions = Theme.transparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton create = Theme.primaryButton("CREATE MY PROFILE  →");
        JButton login = Theme.secondaryButton("I ALREADY HAVE AN ACCOUNT");
        create.setPreferredSize(new Dimension(225, 48));
        login.setPreferredSize(new Dimension(245, 48));
        actions.add(create);
        actions.add(login);

        JPanel bottom = Theme.transparentPanel(new BorderLayout(12, 12));
        bottom.add(featureGrid, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);

        left.add(top, BorderLayout.NORTH);
        left.add(bottom, BorderLayout.CENTER);

        JPanel right = Theme.cardPanel(new BorderLayout(12, 12));
        right.setBorder(new EmptyBorder(12, 12, 12, 12));
        JPanel art = new Theme.FitnessImagePanel("assets/fitwise_reference.png");
        right.add(art, BorderLayout.CENTER);
        JLabel artCaption = new JLabel("SMART TRAINING  •  VISUAL FITNESS  •  MEASURABLE PROGRESS");
        artCaption.setFont(new Font("SansSerif", Font.BOLD, 10));
        artCaption.setForeground(Theme.MUTED_TEXT);
        artCaption.setHorizontalAlignment(SwingConstants.CENTER);
        right.add(artCaption, BorderLayout.SOUTH);

        content.add(left);
        content.add(right);
        add(content, BorderLayout.CENTER);

        JLabel footer = new JLabel("FITWISE  /  PERSONALIZED FITNESS INTELLIGENCE  /  BUILD • TRACK • ADAPT");
        footer.setFont(new Font("SansSerif", Font.BOLD, 9));
        footer.setForeground(Theme.MUTED_TEXT);
        add(footer, BorderLayout.SOUTH);

        create.addActionListener(e -> app.showPanel(MainApp.REGISTER));
        login.addActionListener(e -> app.showPanel(MainApp.LOGIN));
    }

    private JPanel feature(String title, String sub) {
        JPanel p = Theme.darkPanel(new BorderLayout(4, 4));
        p.add(Theme.header(title), BorderLayout.NORTH);
        p.add(Theme.mutedLabel(sub), BorderLayout.SOUTH);
        return p;
    }
}
