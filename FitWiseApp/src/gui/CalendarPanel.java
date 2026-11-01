package gui;

import data.DataStore;
import model.Progress;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Monthly adherence calendar for workouts, meals, water, sleep and weight logs. */
public class CalendarPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JPanel grid=new JPanel(new GridLayout(0,7,6,6));
    private final JLabel monthLabel=Theme.header("");
    private final JLabel monthSummary=Theme.mutedLabel("");
    private YearMonth shown=YearMonth.now();

    public CalendarPanel(MainApp a){
        app=a;setOpaque(false);setLayout(new BorderLayout(14,14));
        setBorder(new EmptyBorder(22,28,18,28));
        add(Theme.title("MONTHLY FITNESS TRACKER 🗓"),BorderLayout.NORTH);

        JPanel nav=Theme.darkPanel(new BorderLayout());
        JButton prev=Theme.secondaryButton("← Prev"),next=Theme.secondaryButton("Next →"),today=Theme.secondaryButton("This month");
        prev.addActionListener(e->{shown=shown.minusMonths(1);refresh();});
        next.addActionListener(e->{shown=shown.plusMonths(1);refresh();});
        today.addActionListener(e->{shown=YearMonth.now();refresh();});
        JPanel nb=Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT,6,0));nb.add(prev);nb.add(today);nb.add(next);
        nav.add(monthLabel,BorderLayout.WEST);nav.add(nb,BorderLayout.EAST);
        grid.setOpaque(false);

        JPanel center=Theme.cardPanel(new BorderLayout(10,10));
        center.add(nav,BorderLayout.NORTH);center.add(grid,BorderLayout.CENTER);center.add(monthSummary,BorderLayout.SOUTH);
        add(center,BorderLayout.CENTER);

        JPanel bottom=Theme.darkPanel(new BorderLayout());
        bottom.add(Theme.mutedLabel("🟩 Workout  •  🍽 Meal  •  💧 Water  •  🌙 Sleep  •  🔵 Weight"),BorderLayout.WEST);
        JButton home=Theme.primaryButton("⌂ HOME");home.addActionListener(e->app.showPanel(MainApp.HOME));
        JButton back=Theme.secondaryButton("← PROGRESS");back.addActionListener(e->app.showPanel(MainApp.PROGRESS));
        JPanel right=Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); right.add(home); right.add(back); bottom.add(right,BorderLayout.EAST);add(bottom,BorderLayout.SOUTH);
    }

    public void refresh(){
        grid.removeAll();
        String month=shown.getMonth().toString();
        monthLabel.setText(month.charAt(0)+month.substring(1).toLowerCase()+" "+shown.getYear());

        for(String d:new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"}){
            JLabel l=Theme.mutedLabel(d);l.setHorizontalAlignment(SwingConstants.CENTER);grid.add(l);
        }

        User u=app.getCurrentUser();
        if(u==null){
            monthSummary.setText("Log in to see your personal calendar.");grid.revalidate();grid.repaint();return;
        }

        Set<String> workouts=DataStore.getWorkoutDoneDates(u.getUserId());
        Map<String,Integer> waters=DataStore.getWaterHistory(u.getUserId());
        Map<String,Double> sleeps=DataStore.getSleepHistory(u.getUserId());
        List<Progress> progress=DataStore.getProgressForUser(u.getUserId());
        Set<String> weights=new HashSet<>();for(Progress p:progress)weights.add(p.getDate());

        LocalDate first=shown.atDay(1);
        int blanks=first.getDayOfWeek()==DayOfWeek.SUNDAY?0:first.getDayOfWeek().getValue();
        for(int i=0;i<blanks;i++)grid.add(Theme.transparentPanel(new BorderLayout()));

        int wd=0,md=0,waterDays=0,sleepDays=0,weightDays=0;
        double waterTarget=service.CalorieCalculator.calculateWaterIntakeLitres(u.getWeight());
        for(int day=1;day<=shown.lengthOfMonth();day++){
            LocalDate date=shown.atDay(day);String key=date.toString();
            boolean w=workouts.contains(key);
            boolean meal=DataStore.getMealsLogged(u.getUserId(),key)>0;
            boolean hydrated=waters.getOrDefault(key,0)>=Math.round(waterTarget*1000*.8);
            boolean slept=sleeps.getOrDefault(key,0.0)>=u.getSleepGoalHours()*.75;
            boolean weight=weights.contains(key);
            if(w)wd++;if(meal)md++;if(hydrated)waterDays++;if(slept)sleepDays++;if(weight)weightDays++;
            grid.add(dayCell(day,date.equals(LocalDate.now()),w,meal,hydrated,slept,weight));
        }
        monthSummary.setText(wd+" workout • "+md+" meal log • "+waterDays+" hydration goal • "
                +sleepDays+" sleep goal • "+weightDays+" weight log(s) this month.");
        grid.revalidate();grid.repaint();
    }

    private JPanel dayCell(int day,boolean today,boolean workout,boolean meal,boolean water,boolean sleep,boolean weight){
        JPanel cell=new JPanel(new BorderLayout(2,2));
        cell.setBackground(workout?new Color(35,92,45):new Color(24,29,31));
        cell.setBorder(new LineBorder(today?Theme.ACCENT:new Color(255,255,255,30),today?2:1));
        cell.setPreferredSize(new Dimension(78,70));
        JLabel n=new JLabel(String.valueOf(day));n.setForeground(Theme.TEXT);
        n.setFont(new Font("SansSerif",Font.BOLD,13));n.setBorder(new EmptyBorder(3,5,0,0));cell.add(n,BorderLayout.NORTH);
        StringBuilder marks=new StringBuilder();
        if(workout)marks.append("🟩 ");if(meal)marks.append("🍽 ");if(water)marks.append("💧 ");
        if(sleep)marks.append("🌙 ");if(weight)marks.append("🔵");
        JLabel m=new JLabel(marks.toString());m.setHorizontalAlignment(SwingConstants.CENTER);cell.add(m,BorderLayout.CENTER);
        return cell;
    }
}
