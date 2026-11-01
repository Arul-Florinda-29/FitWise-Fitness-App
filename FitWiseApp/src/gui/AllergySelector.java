package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Small reusable allergy/food-avoidance selector used during profile setup and editing. */
public final class AllergySelector {
    private AllergySelector(){}

    public static String choose(Component parent, String current) {
        int answer=JOptionPane.showConfirmDialog(parent,
                "Do you have any food allergies or foods you need to avoid?",
                "Food allergy check",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(answer==JOptionPane.CANCEL_OPTION || answer==JOptionPane.CLOSED_OPTION)return null;
        if(answer==JOptionPane.NO_OPTION)return "None";

        String[] names={"Peanuts","Tree Nuts","Dairy / Lactose","Egg","Fish / Seafood","Gluten / Wheat","Soy"};
        String normalized=current==null?"":current.toLowerCase();
        JPanel panel=new JPanel(new GridLayout(0,1,4,4));
        JCheckBox[] boxes=new JCheckBox[names.length];
        for(int i=0;i<names.length;i++){
            boxes[i]=new JCheckBox(names[i]);
            boxes[i].setSelected(normalized.contains(names[i].split(" ")[0].toLowerCase())
                    || (names[i].startsWith("Dairy") && (normalized.contains("dairy")||normalized.contains("lactose")))
                    || (names[i].startsWith("Fish") && normalized.contains("fish"))
                    || (names[i].startsWith("Gluten") && (normalized.contains("gluten")||normalized.contains("wheat"))));
            panel.add(boxes[i]);
        }
        JTextField other=new JTextField();
        other.setToolTipText("Example: sesame");
        panel.add(new JLabel("Other food to avoid (optional):"));
        panel.add(other);

        int ok=JOptionPane.showConfirmDialog(parent,panel,"Select allergies / foods to avoid",
                JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(ok!=JOptionPane.OK_OPTION)return null;

        List<String> selected=new ArrayList<>();
        for(JCheckBox b:boxes)if(b.isSelected())selected.add(b.getText());
        if(!other.getText().trim().isEmpty())selected.add(other.getText().trim());
        return selected.isEmpty()?"None":String.join(", ",selected);
    }
}
