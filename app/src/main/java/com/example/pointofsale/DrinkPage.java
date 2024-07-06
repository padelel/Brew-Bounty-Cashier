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

import com.example.pointofsale.adapter.DrinkAdapter;
import com.example.pointofsale.model.Drink;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DrinkPage extends AppCompatActivity {

    Button btnBack;
    SearchView searchView;
    RecyclerView drinkView;
    DatabaseReference dbRef;
    List<Drink> drinklist = new ArrayList<>();
    DrinkAdapter drinkAdapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_page);

        // Inisialisasi RecyclerView dan layout manager
        drinkView = findViewById(R.id.drink_view);
        drinkView.setHasFixedSize(true);
        drinkView.setLayoutManager(new LinearLayoutManager(this));
        drinkAdapter = new DrinkAdapter(getApplicationContext(), drinklist);
        drinkView.setAdapter(drinkAdapter);

        // Inisialisasi Firebase Realtime Database
        dbRef = FirebaseDatabase.getInstance().getReference().child("menu").child("drink");

        // Setup SearchView
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    drinklist.clear();
                    drinkAdapter.notifyDataSetChanged();
                    getData();
                } else {
                    performSearch(newText);
                }
                return false;
            }
        });

        // Setup ProgressDialog
        progressDialog = new ProgressDialog(DrinkPage.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Mengambil data...");

        // Button Back
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(DrinkPage.this, Category.class);
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
                drinklist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String imageURL = snapshot.child("imageURL").getValue(String.class);
                    String price = snapshot.child("price").getValue(String.class);
                    String stock = snapshot.child("stock").getValue(String.class); // Ambil nilai stock sebagai string

                    Drink drink = new Drink(name, category, description, imageURL, price, stock); // Masukkan stock ke constructor
                    drink.setId(id);
                    drinklist.add(drink);
                }
                drinkAdapter.notifyDataSetChanged();
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
                drinklist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String imageURL = snapshot.child("imageURL").getValue(String.class);
                    String price = snapshot.child("price").getValue(String.class);
                    String stock = snapshot.child("stock").getValue(String.class); // Ambil nilai stock sebagai string

                    Drink drink = new Drink(name, category, description, imageURL, price, stock); // Masukkan stock ke constructor
                    drink.setId(id);
                    drinklist.add(drink);
                }
                drinkAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Gagal melakukan pencarian: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
