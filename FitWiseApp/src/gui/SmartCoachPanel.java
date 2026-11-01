package gui;

import data.DataStore;
import model.User;
import service.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SmartCoachPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JLabel insight=Theme.bodyLabel("—"), score=Theme.title("—");
    private final JLabel mlRecommendation=Theme.bodyLabel("—");
    private final JLabel mlConfidence=Theme.mutedLabel("—");

    public SmartCoachPanel(MainApp a){
        app=a; setOpaque(false); setLayout(new BorderLayout(15,15));
        setBorder(new EmptyBorder(24,30,20,30));
        add(Theme.title("✦ SMART COACH"),BorderLayout.NORTH);

        JPanel g=new JPanel(new GridLayout(2,2,12,12)); g.setOpaque(false);
        JPanel i=Theme.cardPanel(new BorderLayout());
        i.add(Theme.header("TODAY'S INTELLIGENCE"),BorderLayout.NORTH); i.add(insight,BorderLayout.CENTER);
        JPanel s=Theme.cardPanel(new BorderLayout());
        s.add(Theme.header("FITNESS SCORE"),BorderLayout.NORTH); s.add(score,BorderLayout.CENTER);

        JPanel m=Theme.cardPanel(new BorderLayout(8,8));
        m.add(Theme.header("WEEKLY EXPERIENCE-BASED PLAN"),BorderLayout.NORTH);
        m.add(mlRecommendation,BorderLayout.CENTER); m.add(mlConfidence,BorderLayout.SOUTH);

        JPanel how=Theme.cardPanel(new GridLayout(0,1,8,8));
        how.add(Theme.header("HOW YOUR PLAN ADAPTS"));
        how.add(Theme.bodyLabel("Weekly review combines your own logs and feedback."));
        how.add(Theme.bodyLabel("Inputs: completed workouts • meals eaten/skipped • weight trend • sleep • difficulty • energy • hunger • meal satisfaction."));
        how.add(Theme.bodyLabel("Transparent rules save a new plan adaptation after your weekly review; no fixed-example KNN voting."));

        g.add(i);g.add(s);g.add(m);g.add(how);add(g,BorderLayout.CENTER);

        JButton back=Theme.secondaryButton("← Home");
        back.addActionListener(e->app.showPanel(MainApp.DASHBOARD)); add(back,BorderLayout.SOUTH);
    }

    public void setVisible(boolean v){super.setVisible(v);if(v)refresh();}

    public void refresh(){
        User u=app.getCurrentUser(); if(u==null)return;
        double bmi=BMICalculator.calculateBMI(u.getWeight(),u.getHeight());
        int st=DataStore.getSteps(u.getUserId()), wa=DataStore.getWaterConsumed(u.getUserId());
        double sleep=DataStore.getSleep(u.getUserId());

        insight.setText("<html><div style='width:430px'><b>"+u.getName()
                +", here's your next best action:</b><br><br>"
                +SmartCoach.getDailyInsight(u,bmi,st,wa)+"</div></html>");
        score.setText(SmartCoach.score(u,st,wa)+" / 100");

        String summary=new AdaptiveRecommendationEngine().getSummary(u);
        mlRecommendation.setText("<html><div style='width:430px'>"+summary.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>")+"</div></html>");
        mlConfidence.setText("Plans update after you submit WEEKLY EXPERIENCE REVIEW on Workout Plan.");
    }
}
