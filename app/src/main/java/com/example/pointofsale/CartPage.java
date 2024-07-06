package com.example.pointofsale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointofsale.adapter.CartAdapter;
import com.example.pointofsale.adapter.CustomerAdapter;
import com.example.pointofsale.model.CartItem;
import com.example.pointofsale.model.Customer;
import com.example.pointofsale.model.Order;
import com.example.pointofsale.model.Pesanan;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartPage extends AppCompatActivity {

    Button btnBack, btnSaveOrder;
    RecyclerView recRecords, customerView;
    List<CartItem> cartItemList;
    List<Customer> customerList = new ArrayList<>();
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    CustomerAdapter customerAdapter;
    CartAdapter cartAdapter;
    SearchView searchView;
    TextView txtTotal;
    ProgressDialog progressDialog;

    double totalHarga = 0; // Menyimpan total harga
    private boolean isCustomerAdded = false;
    private String orderId; // Menyimpan ID pesanan yang baru dibuat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_page);

        btnBack = findViewById(R.id.btnBack);
        btnSaveOrder = findViewById(R.id.btnSaveOrder);
        recRecords = findViewById(R.id.rec_records);
        customerView = findViewById(R.id.customer_view);
        txtTotal = findViewById(R.id.txt_total);
        searchView = findViewById(R.id.searchView);

        // Inisialisasi RecyclerView dan Adapter untuk Cart
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItemList);
        recRecords.setAdapter(cartAdapter);
        recRecords.setLayoutManager(new LinearLayoutManager(this));
        // Fetch data from Realtime Database untuk Cart
        fetchDataFromDatabase();

        // Inisialisasi ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        // Inisialisasi RecyclerView dan Adapter untuk Customer
        customerAdapter = new CustomerAdapter(this, customerList);
        customerAdapter.setDialog(new CustomerAdapter.Dialog() {
            @Override
            public void onClick(int pos) {
                Customer clickedCustomer = customerList.get(pos);
                addCustomerToRecords(clickedCustomer);
            }
        });
        customerView.setAdapter(customerAdapter);
        customerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch data from Realtime Database untuk Customer
        getData();

        // Tombol "Simpan Pesanan"
        btnSaveOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrderToDatabase();
            }
        });

        // Button Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(CartPage.this, TransactionPage.class);
                startActivity(back);
            }
        });

        // SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    customerList.clear();
                    customerAdapter.notifyDataSetChanged();
                    getData();
                } else {
                    performSearch(newText);
                }
                return false;
            }
        });

        // Generate an automatic order ID based on the current timestamp
        orderId = generateAutomaticOrderId();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getData(); // Memuat data pelanggan pada saat aplikasi dimulai
    }

    private void fetchDataFromDatabase() {
        DatabaseReference pesananRef = dbRef.child("pesanan");

        pesananRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItemList.clear();
                totalHarga = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Pesanan pesanan = dataSnapshot.getValue(Pesanan.class);

                    CartItem cartItem = new CartItem(pesanan.getMenu(), pesanan.getHarga(), pesanan.getKuantitas());

                    cartItemList.add(cartItem);

                    totalHarga += (cartItem.getKuantitas() * cartItem.getHarga());
                }

                String formattedTotal = String.format("Rp.%.2f", totalHarga);
                txtTotal.setText(formattedTotal);

                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartPage", "Failed to read data from Realtime Database", error.toException());
            }
        });
    }

    private void getData() {
        progressDialog.show();

        dbRef.child("customer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customerList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Customer customer = dataSnapshot.getValue(Customer.class);
                    customer.setId(dataSnapshot.getKey());
                    customerList.add(customer);
                }
                customerAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Data gagal diambil!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void addCustomerToRecords(Customer customer) {
        if (!isCustomerAdded) {
            CartItem cartItem = new CartItem(customer.getName(), 0, 0);
            cartItemList.add(cartItem);
            isCustomerAdded = true;
            cartAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(CartPage.this, "Hanya satu nama pelanggan yang dapat ditambahkan", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSearch(String query) {
        customerList.clear();

        dbRef.child("customer").orderByChild("name").startAt(query).endAt(query + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Customer customer = dataSnapshot.getValue(Customer.class);
                            customerList.add(customer);
                        }
                        customerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CartPage", "Error getting customer data", error.toException());
                    }
                });
    }

    private void saveOrderToDatabase() {
        DatabaseReference orderRef = dbRef.child("order");

        if (cartItemList.isEmpty()) {
            Toast.makeText(CartPage.this, "Tidak ada pesanan untuk disimpan!", Toast.LENGTH_SHORT).show();
            return;
        }

        isOrderAlreadySaved(result -> {
            if (result) {
                Toast.makeText(CartPage.this, "Pesanan sudah pernah disimpan sebelumnya!", Toast.LENGTH_SHORT).show();
            } else {
                String customerName = cartItemList.get(cartItemList.size() - 1).getMenu();
                double totalPrice = calculateTotalPrice(cartItemList);
                Order order = new Order(customerName, orderId, cartItemList, totalPrice);

                orderId = generateAutomaticOrderId();
                order.setOrderId(orderId);

                orderRef.push().setValue(order)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("CartPage", "Pesanan berhasil disimpan! Order ID: " + orderId);
                                Toast.makeText(CartPage.this, "Pesanan disimpan!", Toast.LENGTH_SHORT).show();

                                clearOrderFromDatabase();

                                totalHarga = 0;
                                String formattedTotal = String.format("Rp.%.2f", totalHarga);
                                txtTotal.setText(formattedTotal);

                                cartItemList.clear();
                                isCustomerAdded = false;
                                cartAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("CartPage", "Gagal menyimpan pesanan!", task.getException());
                                Toast.makeText(CartPage.this, "Gagal menyimpan pesanan!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private double calculateTotalPrice(List<CartItem> cartItems) {
        double totalPrice = 0;
        for (CartItem cartItem : cartItems) {
            totalPrice += cartItem.getKuantitas() * cartItem.getHarga();
        }
        return totalPrice;
    }

    private void isOrderAlreadySaved(final ResultCallback<Boolean> callback) {
        DatabaseReference orderRef = dbRef.child("order");

        orderRef.orderByChild("orderId").equalTo(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isOrderSaved = snapshot.exists();
                callback.onCallback(isOrderSaved);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartPage.this, "Gagal memeriksa pesanan!", Toast.LENGTH_SHORT).show();
                callback.onCallback(false);
            }
        });
    }

    private void updateTotalOrders() {
        DatabaseReference totalOrdersRef = dbRef.child("total_orders");

        totalOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final int[] totalOrders = {snapshot.exists() ? snapshot.getValue(Integer.class) : 0};
                totalOrders[0] += 1;

                totalOrdersRef.setValue(totalOrders[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("CartPage", "Total pesanan diperbarui: " + totalOrders[0]);
                    } else {
                        Log.e("CartPage", "Gagal memperbarui total pesanan", task.getException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartPage", "Gagal memperbarui total pesanan", error.toException());
            }
        });
    }


    private String generateAutomaticOrderId() {
        long timestamp = System.currentTimeMillis();
        String uniqueComponent = "";
        return "ORDER_" + timestamp + "_" + uniqueComponent;
    }

    interface ResultCallback<T> {
        void onCallback(T result);
    }

    private void clearOrderFromDatabase() {
        DatabaseReference pesananRef = dbRef.child("pesanan");

        pesananRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue();
                }

                cartItemList.clear();
                cartAdapter.notifyDataSetChanged();
                Toast.makeText(CartPage.this, "Pesanan dihapus!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartPage.this, "Gagal menghapus pesanan!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
