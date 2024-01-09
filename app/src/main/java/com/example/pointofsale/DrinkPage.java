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

import com.example.pointofsale.adapter.DrinkAdapter;
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

public class DrinkPage extends AppCompatActivity {

    Button btnBack;

    FloatingActionButton btnAdd;
    SearchView searchView;
    RecyclerView drinkView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Drink> drinklist = new ArrayList<>();
    DrinkAdapter drinkAdapter;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_page);
        drinkView = findViewById(R.id.drink_view);
        btnAdd = findViewById(R.id.btnAdd);
        db = FirebaseFirestore.getInstance();

        drinkView.setHasFixedSize(true);
        drinkView.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchView);
        drinklist = new ArrayList<>();


        drinkAdapter = new DrinkAdapter(getApplicationContext(), drinklist);
        drinkView.setAdapter(drinkAdapter);
        drinkAdapter.setDialog(new DrinkAdapter.Dialog() {
            @Override
            public void onClick(int pos) {
                final CharSequence[] dialogItem = {"Edit", "Hapus"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(DrinkPage.this);
                dialog.setItems(dialogItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), AddDrink.class);
                                intent.putExtra("id", drinklist.get(pos).getId());
                                intent.putExtra("product", drinklist.get(pos).getProduct());
                                intent.putExtra("price", drinklist.get(pos).getPrice());
                                startActivity(intent);
                                break;
                            case 1:
                                deleteData(drinklist.get(pos).getId());
                                break;

                        }
                    }
                });
                dialog.show();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        drinkView.setLayoutManager(layoutManager);
        drinkView.addItemDecoration(decoration);
        drinkView.setAdapter(drinkAdapter);

        progressDialog = new ProgressDialog(DrinkPage.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Mengambil data...");

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(DrinkPage.this, Category.class);
                startActivity(back);
            }

        });

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AddDrink.class));
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
                    drinklist.clear();
                    drinkAdapter.notifyDataSetChanged();
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
    private void deleteData(String id) {
        progressDialog.show();
        db.collection("drink").document(id)
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
        drinklist.clear();

        // Jika query tidak kosong, lakukan pencarian
        if (!query.isEmpty()) {
            db.collection("drink")
                    .whereGreaterThanOrEqualTo("product", query)
                    .whereLessThanOrEqualTo("product", query + "\uf8ff") // \uf8ff adalah karakter Unicode yang digunakan untuk melakukan pencarian rentang
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Drink drink = document.toObject(Drink.class);
                                drinklist.add(drink);
                            }
                            drinkAdapter.notifyDataSetChanged();
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