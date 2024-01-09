package com.example.pointofsale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.adapter.FoodAdapter;
import com.example.pointofsale.model.Food;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FoodPage extends AppCompatActivity {

    Button btnBack;

    FloatingActionButton btnAdd;
    SearchView searchView;
    RecyclerView foodView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Food> foodlist = new ArrayList<>();
    FoodAdapter foodAdapter;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_page);
        foodView = findViewById(R.id.food_view);
        btnAdd = findViewById(R.id.btnAdd);
        db = FirebaseFirestore.getInstance();

        foodView.setHasFixedSize(true);
        foodView.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchView);
        foodlist = new ArrayList<>();

        foodAdapter = new FoodAdapter(getApplicationContext(), foodlist);
        foodView.setAdapter(foodAdapter);
        foodAdapter.setDialog(new FoodAdapter.Dialog() {
            @Override
            public void onClick(int pos) {
                final CharSequence[] dialogItem = {"Edit", "Hapus"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(FoodPage.this);
                dialog.setItems(dialogItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), AddFood.class);
                                intent.putExtra("id", foodlist.get(pos).getId());
                                intent.putExtra("product", foodlist.get(pos).getProduct());
                                intent.putExtra("price", foodlist.get(pos).getPrice());
                                startActivity(intent);
                                break;
                            case 1:
                                deleteData(foodlist.get(pos).getId());
                                break;

                        }
                    }
                });
                dialog.show();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        foodView.setLayoutManager(layoutManager);
        foodView.addItemDecoration(decoration);
        foodView.setAdapter(foodAdapter);

        progressDialog = new ProgressDialog(FoodPage.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Mengambil data...");

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(FoodPage.this, Category.class);
                startActivity(back);
            }

        });

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AddFood.class));
        });

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


    }

    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }

    private void getData(){
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
    private void deleteData(String id) {
        progressDialog.show();
        db.collection("food").document(id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Data gagal di hapus!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                        getData();
                    }
                });
    }
    private void performSearch(String query) {
        foodlist.clear();

        // Jika query tidak kosong, lakukan pencarian
        if (!query.isEmpty()) {
            db.collection("food")
                    .whereGreaterThanOrEqualTo("product", query)
                    .whereLessThanOrEqualTo("product", query + "\uf8ff") // \uf8ff adalah karakter Unicode yang digunakan untuk melakukan pencarian rentang
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Food food = document.toObject(Food.class);
                                foodlist.add(food);
                            }
                            foodAdapter.notifyDataSetChanged();
                        } else {
                            // Tangani kegagalan query jika diperlukan
                            // Contoh: Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    });
        } else {
            // Jika query kosong, tampilkan semua data
            getData();
        }
    }
}