package com.example.pointofsale;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.adapter.CustomerAdapter;
import com.example.pointofsale.model.Customer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerPage extends AppCompatActivity {

    Button btnBack;

    FloatingActionButton btnAdd;
    SearchView searchView;
    RecyclerView customerView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Customer> customerlist = new ArrayList<>();
    CustomerAdapter customerAdapter;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_page);
        customerView = findViewById(R.id.customer_view);
        btnAdd = findViewById(R.id.btnAdd);
        db = FirebaseFirestore.getInstance();

        customerView.setHasFixedSize(true);
        customerView.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchView);
        customerlist = new ArrayList<>();


        customerAdapter = new CustomerAdapter(getApplicationContext(), customerlist);
        customerView.setAdapter(customerAdapter);
        customerAdapter.setDialog(new CustomerAdapter.Dialog() {
            @Override
            public void onClick(int pos) {
                final CharSequence[] dialogItem = {"Edit", "Hapus"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(CustomerPage.this);
                dialog.setItems(dialogItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), AddCustomer.class);
                                intent.putExtra("id", customerlist.get(pos).getId());
                                intent.putExtra("name", customerlist.get(pos).getName());
                                intent.putExtra("email", customerlist.get(pos).getEmail());
                                intent.putExtra("address", customerlist.get(pos).getAddress());
                                intent.putExtra("phone", customerlist.get(pos).getPhone());
                                startActivity(intent);
                                break;
                            case 1:
                                deleteData(customerlist.get(pos).getId());
                                break;

                        }
                    }
                });
                dialog.show();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        customerView.setLayoutManager(layoutManager);
        customerView.addItemDecoration(decoration);
        customerView.setAdapter(customerAdapter);

        progressDialog = new ProgressDialog(CustomerPage.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Mengambil data...");

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(CustomerPage.this, HomePage.class);
                startActivity(back);
            }

        });

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AddCustomer.class));
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
                    customerlist.clear();
                    customerAdapter.notifyDataSetChanged();
                    getData();
                }
                else {
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
        db.collection("customer")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        customerlist.clear();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Customer customer = new Customer(document.getString("name"), document.getString("email"), document.getString("address"), document.getString("phone"));
                                customer.setId(document.getId());
                                customerlist.add(customer);
                            }
                            customerAdapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(getApplicationContext(), "Data gagal di ambil!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }
    private void deleteData(String id) {
        progressDialog.show();
        db.collection("customer").document(id)
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
        customerlist.clear();

        // Jika query tidak kosong, lakukan pencarian
        if (!query.isEmpty()) {
            db.collection("customer")
                    .whereGreaterThanOrEqualTo("name", query)
                    .whereLessThanOrEqualTo("name", query + "\uf8ff") // \uf8ff adalah karakter Unicode yang digunakan untuk melakukan pencarian rentang
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Customer customer = document.toObject(Customer.class);
                                customerlist.add(customer);
                            }
                            customerAdapter.notifyDataSetChanged();
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