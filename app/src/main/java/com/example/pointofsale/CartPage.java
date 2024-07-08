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
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
    private Customer selectedCustomer; // Menyimpan data customer yang dipilih

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
                selectedCustomer = customerList.get(pos); // Menyimpan customer yang dipilih
                Toast.makeText(CartPage.this, "Customer " + selectedCustomer.getName() + " selected", Toast.LENGTH_SHORT).show();
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
                if (selectedCustomer == null) {
                    Toast.makeText(CartPage.this, "Pilih customer terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else {
                    saveOrderToDatabase();
                }
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

                    // Periksa urutan parameter di sini, pastikan `harga` dan `kuantitas` dalam urutan yang benar
                    CartItem cartItem = new CartItem(pesanan.getMenu(), pesanan.getKuantitas(), pesanan.getHarga());

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

        dbRef.child("customers").addListenerForSingleValueEvent(new ValueEventListener() {
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

        dbRef.child("customers").orderByChild("name").startAt(query).endAt(query + "\uf8ff")
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

    private List<Order.CartItem> convertToOrderCartItemList(List<CartItem> cartItems) {
        List<Order.CartItem> orderCartItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Order.CartItem orderCartItem = new Order.CartItem(
                    cartItem.getMenu(),
                    cartItem.getKuantitas(),
                    cartItem.getHarga()
            );
            orderCartItems.add(orderCartItem);
        }

        return orderCartItems;
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
                String customerName = selectedCustomer.getName(); // Menggunakan nama customer yang dipilih
                double totalPrice = calculateTotalPrice(cartItemList);
                List<Order.CartItem> orderCartItems = convertToOrderCartItemList(cartItemList);
                Order order = new Order(customerName, orderId, orderCartItems, totalPrice);

                orderId = generateAutomaticOrderId();
                order.setOrderId(orderId);

                orderRef.push().setValue(order)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("CartPage", "Pesanan berhasil disimpan! Order ID: " + orderId);
                                Toast.makeText(CartPage.this, "Pesanan disimpan!", Toast.LENGTH_SHORT).show();

                                // Kurangi stok item dari menu
                                reduceStock();

                                clearOrderFromDatabase();

                                totalHarga = 0;
                                String formattedTotal = String.format("Rp.%.2f", totalHarga);
                                txtTotal.setText(formattedTotal);

                                cartItemList.clear();
                                isCustomerAdded = false;
                                selectedCustomer = null;
                                cartAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("CartPage", "Gagal menyimpan pesanan!", task.getException());
                                Toast.makeText(CartPage.this, "Pesanan gagal disimpan!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void reduceStock() {
        for (CartItem cartItem : cartItemList) {
            String menuName = cartItem.getMenu();
            int quantity = cartItem.getKuantitas();

            // Kurangi stok untuk item food
            DatabaseReference foodRef = dbRef.child("menu").child("food");
            Query foodQuery = foodRef.orderByChild("name").equalTo(menuName);
            foodQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Integer currentStock = null;
                        try {
                            currentStock = dataSnapshot.child("stock").getValue(Integer.class);
                        } catch (DatabaseException e) {
                            // Jika konversi langsung ke Integer gagal, coba konversi dari String ke Integer
                            String stockString = dataSnapshot.child("stock").getValue(String.class);
                            if (stockString != null) {
                                currentStock = Integer.parseInt(stockString);
                            }
                        }

                        if (currentStock != null) {
                            int newStock = currentStock - quantity;
                            String newStockString = String.valueOf(newStock);
                            dataSnapshot.getRef().child("stock").setValue(newStockString);
                        } else {
                            Log.e("CartPage", "Stock is null or invalid for food item: " + menuName);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CartPage", "Gagal mengupdate stok food", error.toException());
                }
            });

            // Kurangi stok untuk item drink
            DatabaseReference drinkRef = dbRef.child("menu").child("drink");
            Query drinkQuery = drinkRef.orderByChild("name").equalTo(menuName);
            drinkQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Integer currentStock = null;
                        try {
                            currentStock = dataSnapshot.child("stock").getValue(Integer.class);
                        } catch (DatabaseException e) {
                            // Jika konversi langsung ke Integer gagal, coba konversi dari String ke Integer
                            String stockString = dataSnapshot.child("stock").getValue(String.class);
                            if (stockString != null) {
                                currentStock = Integer.parseInt(stockString);
                            }
                        }

                        if (currentStock != null) {
                            int newStock = currentStock - quantity;
                            String newStockString = String.valueOf(newStock);
                            dataSnapshot.getRef().child("stock").setValue(newStockString);
                        } else {
                            Log.e("CartPage", "Stock is null or invalid for drink item: " + menuName);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CartPage", "Gagal mengupdate stok drink", error.toException());
                }
            });
        }
    }

    private void isOrderAlreadySaved(final OrderCheckCallback callback) {
        DatabaseReference orderRef = dbRef.child("order");
        orderRef.orderByChild("orderId").equalTo(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onOrderCheck(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartPage", "Gagal memeriksa status pesanan!", error.toException());
            }
        });
    }

    private void clearOrderFromDatabase() {
        DatabaseReference pesananRef = dbRef.child("pesanan");

        pesananRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue();
                }
                Log.d("CartPage", "Pesanan berhasil dihapus dari Realtime Database.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartPage", "Gagal menghapus pesanan dari Realtime Database", error.toException());
            }
        });
    }

    private String generateAutomaticOrderId() {
        return String.valueOf(System.currentTimeMillis());
    }

    private double calculateTotalPrice(List<CartItem> cartItems) {
        double totalPrice = 0.0;
        for (CartItem item : cartItems) {
            totalPrice += item.getHarga() * item.getKuantitas();
        }
        return totalPrice;
    }

    // Callback interface for order check
    private interface OrderCheckCallback {
        void onOrderCheck(boolean exists);
    }
}
