package gui;

import data.DataStore;
import model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SleepRecoveryPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JLabel logged = Theme.title("0.0 h");
    private final JLabel target = Theme.header("Target —");
    private final JLabel score = Theme.title("— / 100");
    private final JSlider quality = new JSlider(1,5,4);
    private final JTextField hours = new JTextField();

    public SleepRecoveryPanel(MainApp a){
        app=a; setOpaque(false); setLayout(new BorderLayout(16,16)); setBorder(new EmptyBorder(24,30,20,30));
        add(Theme.title("SLEEP & RECOVERY INTELLIGENCE 🌙"),BorderLayout.NORTH);

        JPanel body=new JPanel(new GridLayout(1,2,14,14));body.setOpaque(false);
        JPanel summary=Theme.cardPanel(new GridLayout(3,1,10,10));
        summary.add(block("LAST SLEEP",logged,"hours logged today"));
        summary.add(block("TARGET",target,"from your profile"));
        summary.add(block("RECOVERY SCORE",score,"sleep duration + quality"));

        JPanel form=Theme.cardPanel(new GridBagLayout());
        GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;
        c.gridy=0;c.insets=new Insets(4,0,7,0);form.add(Theme.header("DAILY CHECK-IN"),c);
        c.gridy++;form.add(Theme.mutedLabel("How many hours did you sleep last night?"),c);
        Theme.styleTextField(hours);hours.setPreferredSize(new Dimension(280,44));c.gridy++;c.insets=new Insets(6,0,16,0);form.add(hours,c);
        c.gridy++;c.insets=new Insets(0,0,5,0);form.add(Theme.mutedLabel("Sleep quality (1 = poor, 5 = excellent)"),c);
        quality.setMajorTickSpacing(1);quality.setPaintTicks(true);quality.setPaintLabels(true);quality.setOpaque(false);quality.setForeground(Theme.TEXT);c.gridy++;c.insets=new Insets(0,0,18,0);form.add(quality,c);
        JButton save=Theme.primaryButton("SAVE SLEEP CHECK-IN  →");c.gridy++;c.insets=new Insets(5,0,0,0);form.add(save,c);
        save.addActionListener(e->save());
        body.add(summary);body.add(form);add(body,BorderLayout.CENTER);
        JButton home=Theme.primaryButton("⌂ HOME");home.addActionListener(e->app.showPanel(MainApp.HOME));JPanel nav=Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT)); nav.add(home); add(nav,BorderLayout.SOUTH);
    }
    private JPanel block(String a,JLabel b,String sub){JPanel p=Theme.darkPanel(new BorderLayout(3,3));p.add(Theme.mutedLabel(a),BorderLayout.NORTH);p.add(b,BorderLayout.CENTER);p.add(Theme.mutedLabel(sub),BorderLayout.SOUTH);return p;}
    public void refresh(){User u=app.getCurrentUser();if(u==null)return;double h=DataStore.getSleep(u.getUserId());logged.setText(String.format("%.1f h",h));target.setText(String.format("Target %.1f h",u.getSleepGoalHours()));int s=(int)Math.round(Math.min(100,(h/Math.max(.1,u.getSleepGoalHours()))*75 + (quality.getValue()/5.0)*25));score.setText(s+" / 100");hours.setText(h>0?String.format("%.1f",h):"");}
    private void save(){try{User u=app.getCurrentUser();double h=Double.parseDouble(hours.getText().trim());if(h<0||h>16)throw new Exception();DataStore.setSleep(u.getUserId(),h);refresh();JOptionPane.showMessageDialog(this,"Sleep check-in saved. Smart Coach will use it for recovery recommendations.");}catch(Exception e){JOptionPane.showMessageDialog(this,"Enter sleep between 0 and 16 hours.");}}
    public void setVisible(boolean v){super.setVisible(v);if(v)refresh();}
}
