package com.example.pointofsale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomePage extends AppCompatActivity {

    CardView Customer, Transaction, Menu, Order;

    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        Customer = findViewById(R.id.cvcustomer);
        Transaction = findViewById(R.id.cvtransaction);
        Menu = findViewById(R.id.cvmenu);
        Order = findViewById(R.id.cvorder);

        Customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(HomePage.this, CustomerPage.class);
                startActivity(open);
            }

        });

        Transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(HomePage.this, TransactionPage.class);
                startActivity(open);
            }

        });
        Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(HomePage.this, Category.class);
                startActivity(open);
            }

        });
        Order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(HomePage.this, OrderActivity.class);
                startActivity(open);
            }
        });

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(HomePage.this, MainActivity.class);
                startActivity(back);
            }

        });
    }
}