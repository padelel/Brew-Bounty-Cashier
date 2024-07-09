package com.example.pointofsale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.pointofsale.model.Pesanan;
import com.example.pointofsale.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CartPage extends AppCompatActivity implements CartAdapter.CartAdapterListener {

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
        cartAdapter = new CartAdapter(cartItemList, this);
        recRecords.setAdapter(cartAdapter);
        recRecords.setLayoutManager(new LinearLayoutManager(this));
        // Fetch data from Realtime Database untuk Cart
        fetchDataFromDatabase();

        // Inisialisasi ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        // Inisialisasi RecyclerView dan Adapter untuk Customer
        customerAdapter = new CustomerAdapter(this, customerList);
        customerAdapter.setDialog(pos -> {
            selectedCustomer = customerList.get(pos); // Menyimpan customer yang dipilih
            Toast.makeText(CartPage.this, "Customer " + selectedCustomer.getName() + " selected", Toast.LENGTH_SHORT).show();
            // Tidak ada penambahan customer ke dalam cartItemList
        });

        customerView.setAdapter(customerAdapter);
        customerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch data from Realtime Database untuk Customer
        getData();

        // Tombol "Simpan Pesanan"
        btnSaveOrder.setOnClickListener(v -> {
            if (selectedCustomer == null) {
                Toast.makeText(CartPage.this, "Pilih customer terlebih dahulu", Toast.LENGTH_SHORT).show();
            } else {
                saveOrderToDatabase();
            }
        });

        // Button Back
        btnBack.setOnClickListener(v -> {
            Intent back = new Intent(CartPage.this, TransactionPage.class);
            startActivity(back);
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

                    // Periksa apakah item sudah ada di dalam list
                    if (!isItemInCart(pesanan.getMenu(), dataSnapshot.getKey())) { // Assuming getKey() gives the unique ID
                        // Pastikan urutan parameter `menu`, `kuantitas`, dan `harga` sesuai dengan konstruktor CartItem
                        CartItem cartItem = new CartItem(dataSnapshot.getKey(), pesanan.getMenu(), pesanan.getKuantitas(), pesanan.getHarga());
                        cartItemList.add(cartItem);

                        totalHarga += (cartItem.getKuantitas() * cartItem.getHarga());
                    } else {
                        // Notifikasi bahwa item sudah ada dalam cart
                        Toast.makeText(CartPage.this, pesanan.getMenu() + " sudah ada dalam keranjang", Toast.LENGTH_SHORT).show();
                    }
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

// Metode untuk mengecek apakah item sudah ada di dalam cart
    private boolean isItemInCart(String menu, String id) {
        for (CartItem item : cartItemList) {
            // Membandingkan baik nama menu maupun ID item
            if (item.getMenu().equals(menu) && item.getId().equals(id)) {
                return true;
            }
        }
        return false;
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

                // Panggil reduceStock sebelum menyimpan pesanan
                reduceStock(cartItemList, () -> {
                    // Simpan pesanan ke database
                    orderRef.push().setValue(order)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Hapus node pesanan
                                    clearPesananNode();

                                    // Bersihkan cart dan perbarui tampilan
                                    cartItemList.clear();
                                    cartAdapter.notifyDataSetChanged();

                                    // Reset total harga
                                    totalHarga = 0;
                                    txtTotal.setText("Rp.0.00");

                                    Toast.makeText(CartPage.this, "Pesanan berhasil disimpan!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("CartPage", "Error saving order", task.getException());
                                    Toast.makeText(CartPage.this, "Gagal menyimpan pesanan!", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            }
        });
    }


    private void reduceStock(List<CartItem> cartItems, final OnStockReducedListener listener) {
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference("menu");

        AtomicInteger remainingItems = new AtomicInteger(cartItems.size()); // Menyimpan jumlah item yang masih harus diproses

        for (CartItem cartItem : cartItems) {
            final String itemName = cartItem.getMenu();

            // Check drink items
            menuRef.child("drink").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot drinkSnapshot) {
                    boolean itemFound = false;

                    for (DataSnapshot itemSnapshot : drinkSnapshot.getChildren()) {
                        String name = itemSnapshot.child("name").getValue(String.class);
                        if (name != null && name.equals(itemName)) {
                            updateStock(itemSnapshot.getRef(), itemSnapshot, cartItem, remainingItems, listener);
                            itemFound = true;
                            break;
                        }
                    }

                    if (!itemFound) {
                        // Check food items if not found in drink items
                        menuRef.child("food").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot foodSnapshot) {
                                for (DataSnapshot itemSnapshot : foodSnapshot.getChildren()) {
                                    String name = itemSnapshot.child("name").getValue(String.class);
                                    if (name != null && name.equals(itemName)) {
                                        updateStock(itemSnapshot.getRef(), itemSnapshot, cartItem, remainingItems, listener);
                                        return;
                                    }
                                }

                                Log.e("CartPage", "Item not found in menu: " + itemName);
                                Toast.makeText(CartPage.this, "Item tidak ditemukan dalam menu!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("CartPage", "Failed to read item from food menu", error.toException());
                                Toast.makeText(CartPage.this, "Gagal membaca item dari menu!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CartPage", "Failed to read item from drink menu", error.toException());
                    Toast.makeText(CartPage.this, "Gagal membaca item dari menu!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateStock(DatabaseReference itemRef, DataSnapshot snapshot, CartItem cartItem, AtomicInteger remainingItems, OnStockReducedListener listener) {
        // Mengambil stock sebagai String
        String stockStr = snapshot.child("stock").getValue(String.class);

        // Mengonversi stock ke Integer
        int currentStock = Integer.parseInt(stockStr);

        int updatedStock = currentStock - cartItem.getKuantitas();

        itemRef.child("stock").setValue(String.valueOf(updatedStock))
                .addOnCompleteListener(task -> {
                    remainingItems.getAndDecrement();

                    if (remainingItems.get() == 0) {
                        listener.onStockReduced();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartPage", "Failed to reduce stock for item: " + cartItem.getMenu(), e);
                    Toast.makeText(CartPage.this, "Gagal mengurangi stok!", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearPesananNode() {
        DatabaseReference pesananRef = dbRef.child("pesanan");
        pesananRef.removeValue();
    }

    private void isOrderAlreadySaved(final OnCheckOrderSavedListener listener) {
        DatabaseReference orderRef = dbRef.child("order");

        Query query = orderRef.orderByChild("orderId").equalTo(orderId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onCheckOrderSaved(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartPage", "Error checking if order already saved", error.toException());
                listener.onCheckOrderSaved(false);
            }
        });
    }

    private String generateAutomaticOrderId() {
        long timestamp = System.currentTimeMillis();
        return "ORD" + timestamp;
    }

    private double calculateTotalPrice(List<CartItem> cartItems) {
        double totalPrice = 0;

        for (CartItem cartItem : cartItems) {
            totalPrice += cartItem.getHarga() * cartItem.getKuantitas();
        }

        return totalPrice;
    }


    @Override
    public void onItemQuantityChanged(CartItem cartItem, int newQuantity) {
        // Cari item yang diubah di dalam cartItemList
        for (CartItem item : cartItemList) {
            if (item.getMenu().equals(cartItem.getMenu())) {
                // Hitung perbedaan kuantitas
                int difference = newQuantity - item.getKuantitas();
                // Update kuantitas item
                item.setKuantitas(newQuantity);
                // Perbarui total harga
                totalHarga += (difference * item.getHarga());
                // Format total harga
                String formattedTotal = String.format("Rp.%.2f", totalHarga);
                txtTotal.setText(formattedTotal);

                // Simpan perubahan kuantitas ke database
                saveQuantityToDatabase(item);
                break;
            }
        }
        // Beritahu adapter bahwa data telah berubah
        cartAdapter.notifyDataSetChanged();
    }

    private void saveQuantityToDatabase(CartItem cartItem) {
        DatabaseReference pesananRef = dbRef.child("pesanan");

        // Cari pesanan dengan nama menu yang sesuai dan update kuantitas
        pesananRef.orderByChild("menu").equalTo(cartItem.getMenu()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().child("kuantitas").setValue(cartItem.getKuantitas())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("CartPage", "Kuantitas item berhasil diperbarui di database");
                                } else {
                                    Log.e("CartPage", "Gagal menyimpan kuantitas ke database", task.getException());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartPage", "Error updating quantity in database", error.toException());
            }
        });
    }


    @Override
    public void onItemDeleted(CartItem cartItem) {
        DatabaseReference pesananRef = dbRef.child("pesanan");

        // Cari item dengan nama menu yang sesuai dan hapus dari database
        pesananRef.orderByChild("menu").equalTo(cartItem.getMenu()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Hapus item dari daftar cartItemList
                                    cartItemList.remove(cartItem);

                                    // Perbarui total harga
                                    totalHarga -= (cartItem.getKuantitas() * cartItem.getHarga());
                                    String formattedTotal = String.format("Rp.%.2f", totalHarga);
                                    txtTotal.setText(formattedTotal);

                                    // Beritahu adapter bahwa data telah berubah
                                    cartAdapter.notifyDataSetChanged();

                                    Toast.makeText(CartPage.this, "Item berhasil dihapus dari keranjang!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("CartPage", "Failed to delete item from database", task.getException());
                                    Toast.makeText(CartPage.this, "Gagal menghapus item dari keranjang!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartPage", "Error deleting item from database", error.toException());
                Toast.makeText(CartPage.this, "Gagal menghapus item dari keranjang!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Interface untuk listener saat proses pengurangan stok selesai
    private interface OnStockReducedListener {
        void onStockReduced();
    }

    // Interface untuk listener saat memeriksa apakah pesanan sudah disimpan sebelumnya
    private interface OnCheckOrderSavedListener {
        void onCheckOrderSaved(boolean result);
    }
}
