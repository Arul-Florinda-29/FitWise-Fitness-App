package gui;

import data.DataStore;
import model.*;
import service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Daily personalized diet plan with allergy-safe substitutions and meal logging. */
public class DietPlanPanel extends Theme.BackgroundPanel {
    private final MainApp app;
    private final JPanel list = new JPanel();
    private final DietRecommendation svc = new DietRecommendation();
    private final JLabel total = Theme.subtitle("");
    private final JLabel personalization = Theme.bodyLabel("");

    public DietPlanPanel(MainApp a) {
        app=a; setOpaque(false); setLayout(new BorderLayout(14,14));
        setBorder(new EmptyBorder(22,28,18,28));

        JPanel header=Theme.darkPanel(new BorderLayout(6,6));
        header.add(Theme.title("PERSONALIZED DIET PLAN 🥗"),BorderLayout.NORTH);
        header.add(personalization,BorderLayout.CENTER);
        header.add(total,BorderLayout.SOUTH);
        add(header,BorderLayout.NORTH);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        add(Theme.transparentScrollPane(list),BorderLayout.CENTER);

        JPanel bottom=Theme.transparentPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back=Theme.secondaryButton("← HOME");
        JButton allergies=Theme.secondaryButton("⚠ REVIEW FOOD ALLERGIES");
        allergies.addActionListener(e->showAllergyInfo());
        back.addActionListener(e->app.showPanel(MainApp.HOME));
        bottom.add(allergies); bottom.add(back); add(bottom,BorderLayout.SOUTH);
    }

    public void setVisible(boolean v){super.setVisible(v);if(v)load();}

    public void refresh() { load(); }

    private void load(){
        list.removeAll();
        User u=app.getCurrentUser(); if(u==null)return;
        LocalDate today=LocalDate.now();
        List<MealPlan> plan=svc.getDailyPlanForDay(u,today.getDayOfYear());

        int target=svc.getPersonalizedCalorieTarget(u);

        personalization.setText("<html><b>"+svc.getPersonalizationSummary(u)
                +"</b><br>Diet: "+u.getFoodPreference()
                +" &nbsp; • &nbsp; Goal: "+u.getFitnessGoal()
                +" &nbsp; • &nbsp; Allergy filter: "+(isNone(u.getAllergies())?"None":"ACTIVE — "+u.getAllergies())
                +"</html>");
        JLabel mlInfo=Theme.bodyLabel(svc.getAdaptiveSummary(u));
        JPanel mlCard=Theme.cardPanel(new BorderLayout(5,5));
        mlCard.add(mlInfo,BorderLayout.CENTER);
        list.add(mlCard);
        list.add(Box.createVerticalStrut(8));

        total.setText("Daily target "+target+" kcal  •  Protein "
                +CalorieCalculator.calculateProteinTarget(u.getWeight(),u.getFitnessGoal())
                +" g  •  "+DataStore.getMealsEaten(u.getUserId(),today.toString())+"/"+plan.size()+" meals eaten");

        for(MealPlan m:plan) addMealCard(u,m,today);
        list.revalidate();list.repaint();
    }

    private void addMealCard(User u,MealPlan m,LocalDate date){
        String choice=DataStore.getMealChoice(u.getUserId(),date.toString(),m.getMealType());
        String description=choice==null?m.getDescription():choice;
        String status=DataStore.getMealStatus(u.getUserId(),date.toString(),m.getMealType());

        JPanel card=Theme.cardPanel(new BorderLayout(10,8));
        JPanel info=Theme.darkPanel(new BorderLayout(5,5));
        JLabel type=Theme.header(m.getMealType());
        info.add(type,BorderLayout.NORTH);
        JLabel desc=Theme.bodyLabel("<html><b>"+description+"</b><br><span style='color:#9aa2ad'>"+m.getCalories()+" kcal • Allergy-safe • Matched to your diet</span></html>");
        info.add(desc,BorderLayout.CENTER);
        JLabel state=Theme.mutedLabel("Status: "+pretty(status));
        info.add(state,BorderLayout.SOUTH);
        card.add(info,BorderLayout.CENTER);

        JPanel actions=Theme.transparentPanel(new GridLayout(0,1,5,5));
        JButton eaten=Theme.primaryButton(status.equals("EATEN")?"✓ EATEN":"MARK EATEN");
        JButton replace=Theme.secondaryButton("I CAN'T TAKE THIS");
        JButton skip=Theme.secondaryButton(status.equals("SKIPPED")?"SKIPPED":"SKIP TODAY");

        eaten.addActionListener(e->{
            DataStore.setMealStatus(u.getUserId(),date.toString(),m.getMealType(),"EATEN");
            load();
        });
        skip.addActionListener(e->{
            DataStore.setMealStatus(u.getUserId(),date.toString(),m.getMealType(),"SKIPPED");
            load();
        });
        replace.addActionListener(e->chooseReplacement(u,m,date));

        actions.add(eaten);actions.add(replace);actions.add(skip);
        card.add(actions,BorderLayout.EAST);
        list.add(card);list.add(Box.createVerticalStrut(9));
    }

    private void chooseReplacement(User u,MealPlan meal,LocalDate date){
        List<Food> options=svc.getAlternativeFoods(u,meal.getMealType(),date.getDayOfYear(),meal.getDescription());
        if(options.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "No safe alternative was found for this meal with your current diet and allergy filters.",
                    "No safe replacement",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] labels=new String[Math.min(6,options.size())];
        for(int i=0;i<labels.length;i++){
            Food f=options.get(i);
            labels[i]=f.getFoodName()+"  •  "+(int)f.getCalories()+" kcal  •  "+(int)f.getProtein()+"g protein";
        }
        String selected=(String)JOptionPane.showInputDialog(this,
                "Choose an allergy-safe replacement for "+meal.getMealType()+":\n"
                        +"These options match "+u.getFoodPreference()+" and your allergy settings.",
                "Choose replacement",JOptionPane.PLAIN_MESSAGE,null,labels,labels[0]);
        if(selected==null)return;

        int index=-1;
        for(int i=0;i<labels.length;i++)if(labels[i].equals(selected)){index=i;break;}
        Food f=options.get(index);
        String replacement=f.getFoodName()+" with vegetables";
        DataStore.setMealChoice(u.getUserId(),date.toString(),meal.getMealType(),replacement);
        DataStore.setMealStatus(u.getUserId(),date.toString(),meal.getMealType(),"PENDING");
        load();
    }

    private void showAllergyInfo(){
        User u=app.getCurrentUser();
        JOptionPane.showMessageDialog(this,
                "Current allergy / avoid list: "+(u==null?"None":u.getAllergies())
                        +"\n\nUnsafe foods are removed from recommendations. If a listed food is unavailable, use "
                        +"I CAN'T TAKE THIS to choose another safe option.\n\nFor medical allergies, always verify ingredients and consult a qualified professional.",
                "Food safety filter",JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean isNone(String s){return s==null||s.trim().isEmpty()||s.equalsIgnoreCase("none");}
    private String pretty(String s){
        if("EATEN".equals(s))return "Eaten ✓";
        if("SKIPPED".equals(s))return "Skipped";
        return "Not logged";
    }
}
