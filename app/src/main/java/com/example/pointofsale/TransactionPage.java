package com.example.pointofsale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.pointofsale.adapter.DrinkAdapter;
import com.example.pointofsale.adapter.FoodAdapter;
import com.example.pointofsale.model.Drink;
import com.example.pointofsale.model.Food;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TransactionPage extends AppCompatActivity {

    Button btnBack;
    FloatingActionButton fabCart;
    RecyclerView foodView;
    RecyclerView drinkView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Food> foodlist = new ArrayList<>();
    List<Drink> drinklist = new ArrayList<>();
    FoodAdapter foodAdapter;
    DrinkAdapter drinkAdapter;

    ProgressDialog progressDialog;

    private void openFoodMenuDetailActivity(Food selectedFood) {
        Intent intent = new Intent(TransactionPage.this, MenuDetailActivity.class);
        intent.putExtra("name", selectedFood.getProduct());
        intent.putExtra("price", selectedFood.getPrice());
        startActivity(intent);
    }

    private void openDrinkMenuDetailActivity(Drink selectedDrink) {
        Intent intent = new Intent(TransactionPage.this, MenuDetailActivity.class);
        intent.putExtra("name", selectedDrink.getProduct());
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
        db = FirebaseFirestore.getInstance();

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

// ...

    }
    @Override
    protected void onStart() {
        super.onStart();
        getDrinkData();
        getFoodData();
    }

    private void getDrinkData(){
        progressDialog.show();
        db.collection("drink")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        drinklist.clear();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Drink drink = new Drink(document.getString("product"), document.getString("price"));
                                drink.setId(document.getId());
                                drinklist.add(drink);
                            }
                            drinkAdapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(getApplicationContext(), "Data gagal di ambil!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void getFoodData(){
        progressDialog.show();
        db.collection("food")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        foodlist.clear();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Food food = new Food(document.getString("product"), document.getString("price"));
                                food.setId(document.getId());
                                foodlist.add(food);
                            }
                            foodAdapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(getApplicationContext(), "Data gagal di ambil!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });


    }
}