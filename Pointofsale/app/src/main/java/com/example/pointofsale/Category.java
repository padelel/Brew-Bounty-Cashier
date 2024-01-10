package com.example.pointofsale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Category extends AppCompatActivity {

    CardView Food, Drink;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Food = findViewById(R.id.cvfood);
        Drink = findViewById(R.id.cvdrink);

        Food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(Category.this, FoodPage.class);
                startActivity(open);
            }

        });

        Drink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(Category.this, DrinkPage.class);
                startActivity(open);
            }

        });

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(Category.this, HomePage.class);
                startActivity(back);
            }

        });
    }
}