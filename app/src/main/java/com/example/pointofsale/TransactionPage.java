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

    private void openFoodMenuDetailActivity(Food selectedFood) {
        Intent intent = new Intent(TransactionPage.this, MenuDetailActivity.class);
        intent.putExtra("name", selectedFood.getName());
        intent.putExtra("price", selectedFood.getPrice());
        intent.putExtra("category", selectedFood.getCategory());
        intent.putExtra("description", selectedFood.getDescription());
        intent.putExtra("imageUrl", selectedFood.getImageURL());
        startActivity(intent);
    }

    private void openDrinkMenuDetailActivity(Drink selectedDrink) {
        Intent intent = new Intent(TransactionPage.this, MenuDetailActivity.class);
        intent.putExtra("name", selectedDrink.getName());
        intent.putExtra("price", selectedDrink.getPrice());
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_page);
        btnBack = findViewById(R.id.btnBack);
        fabCart = findViewById(R.id.fabCart);

        foodView = findViewById(R.id.rec_food);
        drinkView = findViewById(R.id.rec_drinks);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        drinkView.setHasFixedSize(true);
        drinklist = new ArrayList<>();

        foodView.setHasFixedSize(true);
        foodlist = new ArrayList<>();

        drinkAdapter = new DrinkAdapter(getApplicationContext(), drinklist);
        drinkView.setAdapter(drinkAdapter);

        // Set the layoutManager and decoration for drinkView
        RecyclerView.LayoutManager layoutManagerDrink = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decorationDrink = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        drinkView.setLayoutManager(layoutManagerDrink);
        drinkView.addItemDecoration(decorationDrink);

        foodAdapter = new FoodAdapter(getApplicationContext(), foodlist);
        foodView.setAdapter(foodAdapter);

        // Set the layoutManager and decoration for foodView
        RecyclerView.LayoutManager layoutManagerFood = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decorationFood = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        foodView.setLayoutManager(layoutManagerFood);
        foodView.addItemDecoration(decorationFood);

        progressDialog = new ProgressDialog(TransactionPage.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Mengambil data...");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(TransactionPage.this, HomePage.class);
                startActivity(back);
            }
        });

        foodAdapter.setOnItemClickListener(new FoodAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Food selectedFood = foodlist.get(position);
                openFoodMenuDetailActivity(selectedFood);
            }
        });

        // Set up OnClickListener for DrinkAdapter
        drinkAdapter.setOnItemClickListener(new DrinkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Drink selectedDrink = drinklist.get(position);
                openDrinkMenuDetailActivity(selectedDrink);
            }
        });

        fabCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Di sini, Anda perlu menentukan cara pengguna memilih item dan kuantitasnya
                // Saya asumsikan bahwa foodlist dan drinklist adalah item yang mungkin dipilih
                ArrayList<String> selectedMenus = new ArrayList<>();
                ArrayList<Integer> selectedQuantities = new ArrayList<>();
                Intent cartIntent = new Intent(TransactionPage.this, CartPage.class);
                cartIntent.putStringArrayListExtra("menus", selectedMenus);
                cartIntent.putIntegerArrayListExtra("quantities", selectedQuantities);
                startActivity(cartIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getDrinkData();
        getFoodData();
    }

    private void getDrinkData() {
        progressDialog.show();
        DatabaseReference drinkRef = databaseReference.child("menu").child("drink");
        drinkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                drinklist.clear();
                for (DataSnapshot drinkSnapshot : snapshot.getChildren()) {
                    Drink drink = drinkSnapshot.getValue(Drink.class);
                    drinklist.add(drink);
                }
                drinkAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Data gagal di ambil!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void getFoodData() {
        progressDialog.show();
        DatabaseReference foodRef = databaseReference.child("menu").child("food");
        foodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodlist.clear();
                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    Food food = foodSnapshot.getValue(Food.class);
                    foodlist.add(food);
                }
                foodAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Data gagal di ambil!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }
}
