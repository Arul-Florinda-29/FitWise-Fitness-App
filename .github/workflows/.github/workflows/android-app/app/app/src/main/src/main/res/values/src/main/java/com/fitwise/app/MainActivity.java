
package com.fitwise.app;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout layout;

    int background = Color.rgb(9, 9, 12);
    int purple = Color.rgb(169, 112, 255);
    int white = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDashboard();
    }

    private void showDashboard() {
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 35, 24, 24);
        layout.setBackgroundColor(background);

        TextView title = new TextView(this);
        title.setText("FitWise");
        title.setTextSize(30);
        title.setTextColor(purple);
        title.setTypeface(null, Typeface.BOLD);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your Personal Fitness & Health Companion");
        subtitle.setTextSize(15);
        subtitle.setTextColor(white);
        subtitle.setPadding(0, 8, 0, 25);
        layout.addView(subtitle);

        addHeading("Welcome to FitWise");

        addButton("BMI Calculator", "BMI");
        addButton("Diet Plan", "DIET");
        addButton("Workout Plan", "WORKOUT");
        addButton("My Profile", "PROFILE");

        TextView footer = new TextView(this);
        footer.setText("Stay Healthy • Stay Strong");
        footer.setTextColor(Color.LTGRAY);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 35, 0, 10);
        layout.addView(footer);

        setContentView(layout);
    }

    private void addHeading(String text) {
        TextView heading = new TextView(this);
        heading.setText(text);
        heading.setTextSize(21);
        heading.setTextColor(white);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, 15, 0, 15);
        layout.addView(heading);
    }

    private void addButton(String text, String type) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(white);
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(purple)
        );

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 58
            );
        params.setMargins(0, 8, 0, 8);

        layout.addView(button, params);

        button.setOnClickListener(v -> {
            if (type.equals("BMI")) {
                showBMI();
            } else {
                android.widget.Toast.makeText(
                    this,
                    text + " screen will be added next",
                    android.widget.Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void showBMI() {
        layout.removeAllViews();
        addHeading("BMI Calculator");

        android.widget.EditText weight = new android.widget.EditText(this);
        weight.setHint("Enter weight in kg");
        weight.setInputType(8194);
        weight.setTextColor(white);
        weight.setHintTextColor(Color.LTGRAY);
        layout.addView(weight);

        android.widget.EditText height = new android.widget.EditText(this);
        height.setHint("Enter height in cm");
        height.setInputType(8194);
        height.setTextColor(white);
        height.setHintTextColor(Color.LTGRAY);
        layout.addView(height);

        Button calculate = new Button(this);
        calculate.setText("Calculate BMI");
        layout.addView(calculate);

        TextView result = new TextView(this);
        result.setTextColor(white);
        result.setTextSize(18);
        result.setPadding(0, 20, 0, 20);
        layout.addView(result);

        calculate.setOnClickListener(v -> {
            try {
                double w = Double.parseDouble(weight.getText().toString());
                double h = Double.parseDouble(height.getText().toString());

                if (w <= 0 || h <= 0) {
                    result.setText("Enter valid positive values.");
                    return;
                }

                double bmi = w / Math.pow(h / 100.0, 2);
                String category;

                if (bmi < 18.5) category = "Underweight";
                else if (bmi < 25) category = "Normal";
                else if (bmi < 30) category = "Overweight";
                else category = "Obese";

                result.setText(
                    String.format(java.util.Locale.US,
                        "Your BMI: %.1f\nCategory: %s", bmi, category)
                );

            } catch (NumberFormatException e) {
                result.setText("Please enter valid numbers.");
            }
        });

        Button back = new Button(this);
        back.setText("Back to Dashboard");
        layout.addView(back);
        back.setOnClickListener(v -> showDashboard());
    }
}
