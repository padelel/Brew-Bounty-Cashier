package com.example.pointofsale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.adapter.DrinkAdapter;
import com.example.pointofsale.adapter.FoodAdapter;
import com.example.pointofsale.model.Drink;
import com.example.pointofsale.model.Food;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TransactionPage extends AppCompatActivity {

    Button btnBack;
    FloatingActionButton fabCart;
    RecyclerView foodView;
    RecyclerView drinkView;
    DatabaseReference databaseReference;
    List<Food> foodlist = new ArrayList<>();
    List<Drink> drinklist = new ArrayList<>();
    FoodAdapter foodAdapter;
    DrinkAdapter drinkAdapter;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_page);

        btnBack = findViewById(R.id.btnBack);
        fabCart = findViewById(R.id.fabCart);
        foodView = findViewById(R.id.rec_food);
        drinkView = findViewById(R.id.rec_drinks);

        // Setup Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Setup RecyclerView for drinks
        drinkView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerDrink = new LinearLayoutManager(this);
        drinkView.setLayoutManager(layoutManagerDrink);
        drinkAdapter = new DrinkAdapter(this, drinklist);
        drinkView.setAdapter(drinkAdapter);
        drinkView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Setup RecyclerView for foods
        foodView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerFood = new LinearLayoutManager(this);
        foodView.setLayoutManager(layoutManagerFood);
        foodAdapter = new FoodAdapter(this, foodlist);
        foodView.setAdapter(foodAdapter);
        foodView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Fetching data...");

        // Button to navigate back to home page
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(TransactionPage.this, HomePage.class);
                startActivity(back);
            }
        });

        // Setup click listener for food items
        foodAdapter.setOnItemClickListener(new FoodAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Food selectedFood = foodlist.get(position);
                openMenuDetailActivity(selectedFood);
            }
        });

        // Setup click listener for drink items
        drinkAdapter.setOnItemClickListener(new DrinkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Drink selectedDrink = drinklist.get(position);
                openMenuDetailActivity(selectedDrink);
            }
        });

        // Floating action button for cart
        fabCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here you would implement logic for adding items to cart
                // For simplicity, assuming you navigate to a cart page
                startActivity(new Intent(TransactionPage.this, CartPage.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Fetch data from Firebase
        fetchDrinkData();
        fetchFoodData();
    }

    private void fetchDrinkData() {
        progressDialog.show();
        DatabaseReference drinkRef = databaseReference.child("menu").child("drink");
        drinkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                drinklist.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Drink drink = dataSnapshot.getValue(Drink.class);
                    drinklist.add(drink);
                }
                drinkAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransactionPage.this, "Failed to retrieve drink data!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void fetchFoodData() {
        progressDialog.show();
        DatabaseReference foodRef = databaseReference.child("menu").child("food");
        foodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodlist.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Food food = dataSnapshot.getValue(Food.class);
                    foodlist.add(food);
                }
                foodAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransactionPage.this, "Failed to retrieve food data!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    // Method to open menu detail activity
    private void openMenuDetailActivity(Food food) {
        Intent intent = new Intent(TransactionPage.this, MenuDetailActivity.class);
        intent.putExtra("name", food.getName());
        intent.putExtra("price", food.getPrice());
        intent.putExtra("category", food.getCategory());
        intent.putExtra("description", food.getDescription());
        intent.putExtra("imageURL", food.getImageURL());
        startActivity(intent);
    }

    // Method to open menu detail activity for drinks
    private void openMenuDetailActivity(Drink drink) {
        Intent intent = new Intent(TransactionPage.this, MenuDetailActivity.class);
        intent.putExtra("name", drink.getName());
        intent.putExtra("price", drink.getPrice());
        startActivity(intent);
    }
}
