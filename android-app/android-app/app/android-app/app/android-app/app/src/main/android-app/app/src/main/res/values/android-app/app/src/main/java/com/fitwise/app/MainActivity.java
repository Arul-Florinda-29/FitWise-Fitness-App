package com.fitwise.app;

import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 40, 24, 24);
        layout.setBackgroundColor(Color.rgb(9, 9, 12));

        TextView title = new TextView(this);
        title.setText("FitWise");
        title.setTextSize(32);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your Personal Fitness Companion");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);
        layout.addView(subtitle);

        addButton("BMI Calculator");
        addButton("Diet Plan");
        addButton("Workout Plan");
        addButton("My Profile");

        setContentView(layout);
    }

    void addButton(String name) {
        Button button = new Button(this);
        button.setText(name);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                Color.rgb(123, 69, 214)
            )
        );

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );

        params.setMargins(0, 20, 0, 0);
        layout.addView(button, params);

        button.setOnClickListener(v ->
            Toast.makeText(this, name + " screen will be added next",
                Toast.LENGTH_SHORT).show()
        );
    }
}
