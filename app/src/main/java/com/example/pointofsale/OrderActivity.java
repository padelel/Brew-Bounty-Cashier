package com.example.pointofsale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.adapter.OrderAdapter;
import com.example.pointofsale.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private DatabaseReference dbRef;
    private ImageView backButton;
    private SearchView searchView;
    private static final String TAG = "OrderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList);
        recyclerView.setAdapter(orderAdapter);

        dbRef = FirebaseDatabase.getInstance().getReference("order");
        fetchDataFromDatabase();

        backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrderActivity.this, HomePage.class);
            startActivity(intent);
            finish();
        });

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterOrders(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterOrders(newText);
                return false;
            }
        });
    }

    private void fetchDataFromDatabase() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null) {
                        orderList.add(order);
                    }
                }
                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error", error.toException());
            }
        });
    }

    void filterOrders(String query) {
        if (query == null) {
            query = ""; // Handle null query
        }

        List<Order> filteredList = new ArrayList<>();
        for (Order order : orderList) {
            String orderId = order.getOrderId();
            String customerName = order.getCustomerName();
            boolean matchesOrderId = orderId != null && orderId.toLowerCase().contains(query.toLowerCase());
            boolean matchesCustomerName = customerName != null && customerName.toLowerCase().contains(query.toLowerCase());

            boolean matchesMenuItem = false;
            if (order.getCartItems() != null) {
                for (Order.CartItem item : order.getCartItems()) {
                    if (item.getMenu() != null && item.getMenu().toLowerCase().contains(query.toLowerCase())) {
                        matchesMenuItem = true;
                        break;
                    }
                }
            }

            if (matchesOrderId || matchesCustomerName || matchesMenuItem) {
                filteredList.add(order);
            }
        }
        orderAdapter.updateList(filteredList);
    }
}