package com.example.pointofsale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.adapter.FoodAdapter;
import com.example.pointofsale.model.Food;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FoodPage extends AppCompatActivity {

    Button btnBack;
    SearchView searchView;
    RecyclerView foodView;
    DatabaseReference dbRef;
    List<Food> foodlist = new ArrayList<>();
    FoodAdapter foodAdapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_page);

        // Inisialisasi RecyclerView dan layout manager
        foodView = findViewById(R.id.food_view);
        foodView.setHasFixedSize(true);
        foodView.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodAdapter(getApplicationContext(), foodlist);
        foodView.setAdapter(foodAdapter);

        // Inisialisasi Firebase Realtime Database
        dbRef = FirebaseDatabase.getInstance().getReference().child("menu").child("food");

        // Setup SearchView
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    foodlist.clear();
                    foodAdapter.notifyDataSetChanged();
                    getData();
                } else {
                    performSearch(newText);
                }
                return false;
            }
        });

        // Setup ProgressDialog
        progressDialog = new ProgressDialog(FoodPage.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Mengambil data...");

        // Button Back
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(FoodPage.this, Category.class);
                startActivity(back);
            }
        });

        // Retrieve data on start
        getData();
    }

    // Method to fetch data from Realtime Database
    private void getData() {
        progressDialog.show();
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                foodlist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String imageURL = snapshot.child("imageURL").getValue(String.class); // Ubah menjadi imageURL
                    String price = snapshot.child("price").getValue(String.class); // Ambil harga sebagai String

                    Food food = new Food(name, category, description, imageURL, price); // Gunakan imageURL
                    food.setId(id);
                    foodlist.add(food);
                }
                foodAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Gagal mengambil data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    // Method untuk melakukan pencarian
    private void performSearch(String query) {
        Query searchQuery = dbRef.orderByChild("name").startAt(query).endAt(query + "\uf8ff");
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                foodlist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String imageURL = snapshot.child("imageURL").getValue(String.class); // Ubah menjadi imageURL
                    String price = snapshot.child("price").getValue(String.class); // Ambil harga sebagai String

                    Food food = new Food(name, category, description, imageURL, price); // Gunakan imageURL
                    food.setId(id);
                    foodlist.add(food);
                }
                foodAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Gagal melakukan pencarian: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
