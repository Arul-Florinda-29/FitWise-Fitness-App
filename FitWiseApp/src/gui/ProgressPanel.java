package gui;

import data.DataStore;
import model.*;
import service.BMICalculator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Progress dashboard: daily adherence + weight/BMI history. */
public class ProgressPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JPanel list=new JPanel();

    public ProgressPanel(MainApp a){
        app=a;setOpaque(false);setLayout(new BorderLayout(14,14));
        setBorder(new EmptyBorder(22,28,18,28));
        add(Theme.title("PROGRESS & DAILY TRACKER 📈"),BorderLayout.NORTH);
        list.setOpaque(false);list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        add(Theme.transparentScrollPane(list),BorderLayout.CENTER);

        JPanel b=Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton log=Theme.primaryButton("+ LOG TODAY'S WEIGHT");
        JButton calendar=Theme.secondaryButton("OPEN MONTHLY CALENDAR →");
        JButton home=Theme.primaryButton("⌂ HOME"); home.addActionListener(e->app.showPanel(MainApp.HOME));
        JButton back=Theme.secondaryButton("← SLEEP TRACKER");
        log.addActionListener(e->logWeight());
        calendar.addActionListener(e->app.showPanel(MainApp.CALENDAR));
        back.addActionListener(e->app.showPanel(MainApp.SLEEP));
        JButton logout=Theme.secondaryButton("LOG OUT"); logout.addActionListener(e->app.logout()); b.add(log);b.add(home);b.add(back);b.add(calendar);b.add(logout);add(b,BorderLayout.SOUTH);
    }

    public void refresh(){
        list.removeAll();
        User u=app.getCurrentUser();if(u==null)return;
        String today=LocalDate.now().toString();

        double bmi=BMICalculator.calculateBMI(u.getWeight(),u.getHeight());
        int step=DataStore.getSteps(u.getUserId());
        double water=DataStore.getWaterConsumed(u.getUserId())/1000.0;
        double waterTarget=service.CalorieCalculator.calculateWaterIntakeLitres(u.getWeight());
        double sleep=DataStore.getSleep(u.getUserId());
        int eaten=DataStore.getMealsEaten(u.getUserId(),today);
        int workout=DataStore.getWorkoutCompletedCount(u.getUserId(),today);

        JPanel hero=new JPanel(new GridLayout(2,4,10,10));hero.setOpaque(false);
        hero.add(metric("BMI",String.format("%.1f • %s",bmi,BMICalculator.getCategory(bmi))));
        hero.add(metric("MEALS",eaten+" / 4","eaten today"));
        hero.add(metric("WORKOUT",workout>0?"Completed":"Not logged","today"));
        hero.add(metric("WATER",String.format("%.1f / %.1f L",water,waterTarget),"today"));
        hero.add(metric("SLEEP",String.format("%.1f / %.1f h",sleep,u.getSleepGoalHours()),"last night"));
        hero.add(metric("STEPS",String.format("%,d / %,d",step,u.getDailyStepGoal()),"today"));
        hero.add(metric("TODAY",today,"activity date"));
        list.add(hero);list.add(Box.createVerticalStrut(12));

        JPanel tracker=Theme.cardPanel(new BorderLayout(8,8));
        tracker.add(Theme.header("TODAY'S ADHERENCE"),BorderLayout.NORTH);
        JPanel bars=new JPanel(new GridLayout(4,2,8,8));bars.setOpaque(false);
        addBar(bars,"Meals eaten",eaten,4);
        addBar(bars,"Workout completed",workout,Math.max(1,workout));
        addBar(bars,"Water",water,waterTarget);
        addBar(bars,"Sleep",sleep,u.getSleepGoalHours());
        tracker.add(bars,BorderLayout.CENTER);
        list.add(tracker);list.add(Box.createVerticalStrut(12));

        List<Progress> p=DataStore.getProgressForUser(u.getUserId());
        double first=p.isEmpty()?u.getWeight():p.get(0).getWeight();
        double latest=p.isEmpty()?u.getWeight():p.get(p.size()-1).getWeight();
        JPanel history=Theme.cardPanel(new BorderLayout(8,8));
        history.add(Theme.header("WEIGHT & BMI HISTORY"),BorderLayout.NORTH);
        JPanel rows=new JPanel();rows.setOpaque(false);rows.setLayout(new BoxLayout(rows,BoxLayout.Y_AXIS));
        if(p.isEmpty())rows.add(Theme.bodyLabel("No weight entries yet. Log today's weight to start your trend."));
        else for(Progress x:p){
            JPanel c=Theme.darkPanel(new BorderLayout());
            c.add(Theme.header(x.getDate()),BorderLayout.WEST);
            c.add(Theme.bodyLabel(String.format("Weight %.1f kg   •   BMI %.1f (%s)",
                    x.getWeight(),x.getBmi(),BMICalculator.getCategory(x.getBmi()))),BorderLayout.CENTER);
            rows.add(c);rows.add(Box.createVerticalStrut(6));
        }
        history.add(rows,BorderLayout.CENTER);
        history.add(Theme.mutedLabel(String.format("Change since first logged entry: %+.1f kg",latest-first)),BorderLayout.SOUTH);
        list.add(history);
        list.revalidate();list.repaint();
    }

    private void addBar(JPanel p,String name,double value,double max){
        JPanel cell=Theme.darkPanel(new BorderLayout(4,4));
        cell.add(Theme.mutedLabel(name),BorderLayout.NORTH);
        JProgressBar bar=new JProgressBar(0,Math.max(1,(int)Math.ceil(max*100)));
        bar.setValue(Math.min(bar.getMaximum(),(int)Math.round(value*100)));
        bar.setStringPainted(true);
        bar.setString(String.format("%.0f%%",Math.min(100,value/Math.max(.01,max)*100)));
        cell.add(bar,BorderLayout.CENTER);p.add(cell);
    }
    private JPanel metric(String a,String b){return metric(a,b,"");}
    private JPanel metric(String a,String b,String sub){
        JPanel p=Theme.darkPanel(new GridLayout(3,1,2,2));
        p.add(Theme.mutedLabel(a));p.add(Theme.header(b));p.add(Theme.mutedLabel(sub));return p;
    }

    private void logWeight(){
        User u=app.getCurrentUser();if(u==null)return;
        String s=JOptionPane.showInputDialog(this,"Current weight (kg):",u.getWeight());
        try{
            double w=Double.parseDouble(s);if(w<=0||w>300)throw new Exception();
            u.setWeight(w);
            double bmi=BMICalculator.calculateBMI(w,u.getHeight());
            DataStore.addProgress(new Progress(u.getUserId(),LocalDate.now().toString(),w,bmi));
            DataStore.saveUserChanges();refresh();app.showPanel(MainApp.DASHBOARD);
        }catch(Exception e){JOptionPane.showMessageDialog(this,"Enter a valid weight.");}
    }
}
