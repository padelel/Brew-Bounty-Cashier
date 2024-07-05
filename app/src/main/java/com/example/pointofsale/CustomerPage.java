package com.example.pointofsale;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomerPage extends AppCompatActivity {

    Button btnBack;
    FloatingActionButton btnAdd;
    SearchView searchView;
    RecyclerView customerView;
    DatabaseReference db;
    List<Customer> customerlist = new ArrayList<>();
    CustomerAdapter customerAdapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_page);
        customerView = findViewById(R.id.customer_view);
        btnAdd = findViewById(R.id.btnAdd);
        db = FirebaseDatabase.getInstance().getReference("customers");

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

        btnAdd.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AddCustomer.class)));

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

    private void getData() {
        progressDialog.show();
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customerlist.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Customer customer = dataSnapshot.getValue(Customer.class);
                    customer.setId(dataSnapshot.getKey());
                    customerlist.add(customer);
                }
                customerAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Data gagal di ambil!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteData(String id) {
        progressDialog.show();
        db.child(id).removeValue().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Data gagal di hapus!", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
            getData();
        });
    }

    private void performSearch(String query) {
        customerlist.clear();

        if (!query.isEmpty()) {
            db.orderByChild("name").startAt(query).endAt(query + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Customer customer = dataSnapshot.getValue(Customer.class);
                        customer.setId(dataSnapshot.getKey());
                        customerlist.add(customer);
                    }
                    customerAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error if necessary
                }
            });
        } else {
            getData();
        }
    }
}
