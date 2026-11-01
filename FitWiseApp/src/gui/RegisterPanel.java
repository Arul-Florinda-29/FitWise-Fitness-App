package gui;

import data.DataStore;
import data.BackendClient;
import java.util.LinkedHashMap;
import java.util.Map;
import model.User;
import service.BMICalculator;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Page 2: account + the personal details required for BMI personalization. */
public class RegisterPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JTextField name = field(), email = field(), age = field(), height = field(), weight = field(), allergy = field();
    private final JPasswordField password = new JPasswordField();
    private final JComboBox<String> gender = box("Male", "Female", "Other");
    private final JComboBox<String> activity = box("Sedentary", "Lightly Active", "Moderately Active", "Very Active");
    private final JComboBox<String> diet = box("Vegetarian", "Non-Vegetarian", "Vegan", "Eggetarian");
    private final JLabel bmiAdvice = Theme.subtitle("Enter height and weight to see your BMI guidance.");

    public RegisterPanel(MainApp a) {
        app=a; setOpaque(false); setLayout(new BorderLayout(15,15)); setBorder(new EmptyBorder(30,55,25,55));
        add(Theme.title("CREATE YOUR FITWISE PROFILE"), BorderLayout.NORTH);

        JPanel form=Theme.cardPanel(new GridLayout(0,2,16,12)); form.setBorder(new EmptyBorder(22,25,22,25));
        form.setPreferredSize(new Dimension(900, 590));
        addField(form,"Full Name",name); addField(form,"Email / Username",email);
        addField(form,"Password",password); addField(form,"Age",age);
        addField(form,"Gender",gender); addField(form,"Height (cm)",height);
        addField(form,"Weight (kg)",weight); addField(form,"Activity Level",activity);
        addField(form,"Food Preference",diet); addField(form,"BMI & HEALTH GUIDANCE",bmiAdvice);

        JPanel allergyBox=new JPanel(new BorderLayout(8,0)); allergyBox.setOpaque(false); allergyBox.add(allergy,BorderLayout.CENTER);
        JButton choose=Theme.secondaryButton("CHOOSE ALLERGIES");
        choose.addActionListener(e->{String s=AllergySelector.choose(this,allergy.getText()); if(s!=null) allergy.setText(s);});
        allergyBox.add(choose,BorderLayout.EAST); addField(form,"Food Allergies / Foods to Avoid",allergyBox);
        javax.swing.event.DocumentListener bmiListener = new javax.swing.event.DocumentListener() {
            private void update() {
                try {
                    double h = Double.parseDouble(height.getText().trim());
                    double w = Double.parseDouble(weight.getText().trim());
                    if (h < 100 || h > 250 || w <= 20 || w > 300) {
                        bmiAdvice.setText("Enter a valid height (100–250 cm) and weight (20–300 kg).");
                        return;
                    }
                    double bmi = BMICalculator.calculateBMI(w, h);
                    bmiAdvice.setText(String.format(java.util.Locale.ROOT,
                        "BMI: %.1f — %s. %s", bmi, BMICalculator.getCategory(bmi),
                        BMICalculator.getRecommendation(bmi)));
                } catch (Exception ignored) {
                    bmiAdvice.setText("Enter height and weight to see your BMI guidance.");
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        };
        height.getDocument().addDocumentListener(bmiListener);
        weight.getDocument().addDocumentListener(bmiListener);

        JScrollPane formScroll=Theme.transparentScrollPane(form);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(20);
        add(formScroll,BorderLayout.CENTER);

        JLabel note=Theme.subtitle("BMI guidance is informational only; discuss health or weight changes with a qualified professional.");
        JPanel bottom=Theme.transparentPanel(new BorderLayout(10,10)); bottom.add(note,BorderLayout.NORTH);
        JPanel buttons=Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back=Theme.secondaryButton("← BACK TO LOGIN"), create=Theme.primaryButton("CREATE ACCOUNT →");
        back.addActionListener(e->app.showPanel(MainApp.LOGIN)); create.addActionListener(e->createAccount());
        buttons.add(back); buttons.add(create); bottom.add(buttons,BorderLayout.SOUTH); add(bottom,BorderLayout.SOUTH);
    }

    private void createAccount(){
        try{
            String n=name.getText().trim(), em=email.getText().trim(), pw=new String(password.getPassword());
            if(n.isEmpty()||em.isEmpty()||pw.isEmpty()) throw new IllegalArgumentException("Name, email and password are required.");
            if (!em.matches("(?i)^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$"))
                throw new IllegalArgumentException("Please enter a valid email address.");
            if (pw.length() < 8) throw new IllegalArgumentException("Password must contain at least 8 characters.");
            if(DataStore.emailExists(em)) throw new IllegalArgumentException("Email / username is already registered.");
            int a=Integer.parseInt(age.getText().trim()); double h=Double.parseDouble(height.getText().trim()); double w=Double.parseDouble(weight.getText().trim());
            if(a<13||a>100||h<100||h>250||w<=20||w>300) throw new IllegalArgumentException("Please enter realistic age, height and weight values.");
            double bmiValue = BMICalculator.calculateBMI(w, h);
            String bmiGuidance = BMICalculator.getAutomaticGoal(bmiValue);
            User u=new User(n,em,pw,a,(String)gender.getSelectedItem(),h,w,w,(String)activity.getSelectedItem(),bmiGuidance,(String)diet.getSelectedItem(),"Full Body",allergy.getText().trim().isEmpty()?"None":allergy.getText().trim(),"Metric",8000,8);
            if(!BackendClient.isAvailable()) throw new IllegalStateException("FitWise backend is not running. Start it using backend\\run-backend.bat, then create your account.");
            Map<String,String> profile=new LinkedHashMap<>();
            profile.put("name",n); profile.put("email",em); profile.put("password",pw); profile.put("age",String.valueOf(a));
            profile.put("gender",u.getGender()); profile.put("height",String.valueOf(h)); profile.put("weight",String.valueOf(w));
            profile.put("activity",u.getActivityLevel()); profile.put("goal",bmiGuidance); profile.put("bmi",String.format(java.util.Locale.ROOT,"%.1f",bmiValue)); profile.put("diet",u.getFoodPreference());
            profile.put("allergies",u.getAllergies()); profile.put("targetWeight",String.valueOf(u.getTargetWeight()));
            profile.put("workout",u.getWorkoutPreference()); profile.put("units",u.getUnits());
            profile.put("stepsGoal",String.valueOf(u.getDailyStepGoal())); profile.put("sleepGoal",String.valueOf(u.getSleepGoalHours()));
            BackendClient.register(profile);
            u.setPassword(""); // Backend stores a salted password hash; do not retain the plain password locally.
            DataStore.addUser(u); app.setCurrentUser(u); app.showPanel(MainApp.ANALYSIS);
        }catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"Age, height and weight must be numbers.","Invalid details",JOptionPane.WARNING_MESSAGE);}
        catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Invalid details",JOptionPane.WARNING_MESSAGE);}
    }
    private JTextField field(){JTextField f=new JTextField(); Theme.styleTextField(f); f.setPreferredSize(new Dimension(300,48)); f.setMinimumSize(new Dimension(100,44)); return f;}
    private JComboBox<String> box(String...v){JComboBox<String> b=new JComboBox<>(v); Theme.styleComboBox(b); b.setPreferredSize(new Dimension(300,48)); b.setMinimumSize(new Dimension(100,44)); return b;}
    private void addField(JPanel p,String label,JComponent c){
        if(c instanceof JTextComponent) Theme.styleTextField((JTextComponent)c);
        if(c instanceof JComboBox){ Theme.styleComboBox((JComboBox<?>)c); c.setPreferredSize(new Dimension(300,48)); c.setMinimumSize(new Dimension(100,44)); }
        JPanel q=Theme.darkPanel(new BorderLayout(6,6)); q.setMinimumSize(new Dimension(240,86));
        q.add(Theme.mutedLabel(label.toUpperCase()),BorderLayout.NORTH); q.add(c,BorderLayout.CENTER); p.add(q);
    }
}
