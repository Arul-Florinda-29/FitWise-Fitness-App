package gui;

import data.DataStore;
import model.Food;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class NutritionPanel extends Theme.BackgroundPanel {
    private final DefaultTableModel model;
    private final JTextField search = new JTextField();
    private final JLabel resultCount = Theme.mutedLabel("");

    public NutritionPanel(MainApp a) {
        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(22, 28, 18, 28));
        add(Theme.title("FOOD INTELLIGENCE DATABASE \uD83E\uDD57"), BorderLayout.NORTH);

        JPanel top = Theme.darkPanel(new FlowLayout(FlowLayout.LEFT));
        Theme.styleTextField(search);
        search.setPreferredSize(new Dimension(220, 34));
        JButton searchBtn = Theme.primaryButton("SEARCH");
        JButton all = Theme.secondaryButton("SHOW ALL");
        top.add(Theme.mutedLabel("Search by food name or diet type (e.g. \"vegan\"):"));
        top.add(search);
        top.add(searchBtn);
        top.add(all);
        top.add(resultCount);

        model = new DefaultTableModel(new Object[]{"Food", "kcal", "Protein", "Carbs", "Fat", "Type"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setBackground(Theme.CARD_BG);
        table.setForeground(Theme.TEXT);
        table.setGridColor(new Color(50, 55, 68));
        table.setRowHeight(28);
        table.getTableHeader().setBackground(Theme.ACCENT_DARK);
        table.getTableHeader().setForeground(Color.WHITE);

        JPanel center = Theme.darkPanel(new BorderLayout(8, 8));
        center.add(top, BorderLayout.NORTH);
        center.add(Theme.transparentScrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JButton back = Theme.secondaryButton("\u2190 More");
        back.addActionListener(e -> a.showPanel(MainApp.MORE));
        add(back, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> load(search.getText()));
        all.addActionListener(e -> { search.setText(""); load(""); });

        // Pressing Enter in the search box now runs the search too (previously only the button worked).
        String enterKey = "fitwise-search-enter";
        search.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), enterKey);
        search.getActionMap().put(enterKey, new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { load(search.getText()); }
        });

        load("");
    }

    private void load(String query) {
        model.setRowCount(0);
        String q = query == null ? "" : query.trim().toLowerCase();
        int count = 0;
        for (Food f : DataStore.getAllFoods()) {
            boolean matches = q.isEmpty()
                    || f.getFoodName().toLowerCase().contains(q)
                    || f.getDietType().toLowerCase().contains(q);
            if (matches) {
                model.addRow(new Object[]{f.getFoodName(), f.getCalories(), f.getProtein() + " g",
                        f.getCarbs() + " g", f.getFat() + " g", f.getDietType()});
                count++;
            }
        }
        resultCount.setText(count + " food" + (count == 1 ? "" : "s") + " found");
    }
}
