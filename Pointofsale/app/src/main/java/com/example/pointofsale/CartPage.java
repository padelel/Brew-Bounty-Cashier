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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.example.pointofsale.adapter.CartAdapter;
import com.example.pointofsale.adapter.CustomerAdapter;
import com.example.pointofsale.model.CartItem;
import com.example.pointofsale.model.Customer;
import com.example.pointofsale.model.Order;
import com.example.pointofsale.model.Pesanan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartPage extends AppCompatActivity {

    Button btnBack, btnSaveOrder;
    RecyclerView recRecords, customerView;
    List<CartItem> cartItemList;
    List<Customer> customerList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        // Fetch data from Firestore untuk Cart
        fetchDataFromFirestore();

        // Inisialisasi ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        // Inisialisasi RecyclerView dan Adapter untuk Cart
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItemList);
        recRecords.setAdapter(cartAdapter);
        recRecords.setLayoutManager(new LinearLayoutManager(this));

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

        // Fetch data from Firestore untuk Cart
        fetchDataFromFirestore();

        // Tombol "Simpan Pesanan"
        btnSaveOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrderToFirestore();
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

    private void fetchDataFromFirestore() {
        CollectionReference pesananRef = db.collection("pesanan");

        pesananRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Bersihkan data yang ada
                    cartItemList.clear();
                    totalHarga = 0;

                    // Iterasi melalui setiap dokumen
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Konversi dokumen Firestore menjadi Pesanan
                        Pesanan pesanan = document.toObject(Pesanan.class);

                        // Buat CartItem dari Pesanan
                        CartItem cartItem = new CartItem(pesanan.getMenu(), pesanan.getHarga(), pesanan.getKuantitas());

                        // Tambahkan item ke cartItemList
                        cartItemList.add(cartItem);

                        // Hitung harga total
                        totalHarga += (cartItem.getKuantitas() * cartItem.getHarga());
                    }

                    // Perbarui TextView dengan harga total yang diformat sebagai mata uang
                    String formattedTotal = String.format("Rp.%.2f", totalHarga);
                    txtTotal.setText(formattedTotal);

                    // Segarkan adapter
                    cartAdapter.notifyDataSetChanged();
                } else {
                    // Tangani kesalahan
                    task.getException().printStackTrace();
                }
            }
        });
    }

    private void getData() {
        progressDialog.show();

        // Mendapatkan data pelanggan dari Firestore
        db.collection("customer")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        customerList.clear();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Customer customer = new Customer(
                                        document.getString("name"),
                                        document.getString("email"),
                                        document.getString("address"),
                                        document.getString("phone")
                                );
                                customer.setId(document.getId());
                                customerList.add(customer);
                            }
                            initCustomerAdapter();
                        } else {
                            Toast.makeText(getApplicationContext(), "Data gagal diambil!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void initCustomerAdapter() {
        customerAdapter.notifyDataSetChanged();
    }

    private void addCustomerToRecords(Customer customer) {
        // Cek apakah pelanggan sudah ditambahkan
        if (!isCustomerAdded) {
            // Jika belum ditambahkan, tambahkan pelanggan ke dalam rec_records
            CartItem cartItem = new CartItem(customer.getName(), 0, 0);
            cartItemList.add(cartItem);

            // Set isCustomerAdded menjadi true
            isCustomerAdded = true;

            // Segarkan adapter rec_records
            cartAdapter.notifyDataSetChanged();
        } else {
            // Jika pelanggan sudah ditambahkan, berikan pesan atau ambil tindakan yang sesuai
            Toast.makeText(CartPage.this, "Hanya satu nama pelanggan yang dapat ditambahkan", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSearch(String query) {
        customerList.clear();

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
                                customerList.add(customer);
                            }
                            customerAdapter.notifyDataSetChanged();
                        } else {
                            // Tangani kegagalan query jika diperlukan
                            // Contoh: Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    });
        } else {
            // Jika query kosong, tampilkan semua data
            initCustomerAdapter();
        }
    }

    private void saveOrderToFirestore() {
        // Mendapatkan referensi ke koleksi "order" di Firestore
        CollectionReference orderRef = db.collection("order");

        // Mengecek apakah ada item di dalam cartItemList
        if (cartItemList.isEmpty()) {
            Toast.makeText(CartPage.this, "Tidak ada pesanan untuk disimpan!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mengecek apakah data pesanan sudah tersimpan di Firestore sebelumnya
        isOrderAlreadySaved(result -> {
            if (result) {
                Toast.makeText(CartPage.this, "Pesanan sudah pernah disimpan sebelumnya!", Toast.LENGTH_SHORT).show();
            } else {
                // Membuat objek Order untuk menyimpan semua data pesanan
                // Assuming the customer's name is in the last cart item
                String customerName = cartItemList.get(cartItemList.size() - 1).getMenu(); // Get customer name from the last cart item
                Order order = new Order(customerName, orderId, cartItemList);

                // Generate an automatic order ID based on the current timestamp
                orderId = generateAutomaticOrderId();
                order.setOrderId(orderId); // Set the generated orderId to the Order object

                // Menambahkan data pesanan ke koleksi "order" di Firestore
                orderRef.add(order)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Pesanan berhasil disimpan ke "order"
                                orderId = task.getResult().getId(); // Menyimpan ID pesanan yang baru dibuat
                                Log.d("CartPage", "Pesanan berhasil disimpan! Order ID: " + orderId);
                                Toast.makeText(CartPage.this, "Pesanan disimpan!", Toast.LENGTH_SHORT).show();

                                // Hapus data dari koleksi "pesanan" di Firestore
                                clearOrderFromFirestore();

                                // Reset total harga menjadi 0
                                totalHarga = 0;
                                // Update TextView dengan harga total yang diformat sebagai mata uang
                                String formattedTotal = String.format("Rp.%.2f", totalHarga);
                                txtTotal.setText(formattedTotal);

                                // Bersihkan rec_records dan refresh tampilan
                                cartItemList.clear();
                                isCustomerAdded = false;
                                cartAdapter.notifyDataSetChanged();
                            } else {
                                // Tangani kesalahan jika gagal menyimpan pesanan
                                Log.e("CartPage", "Gagal menyimpan pesanan!", task.getException());
                                Toast.makeText(CartPage.this, "Gagal menyimpan pesanan!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    // Metode untuk memeriksa apakah pesanan sudah pernah disimpan sebelumnya
    private void isOrderAlreadySaved(final ResultCallback<Boolean> callback) {
        // Mendapatkan referensi ke koleksi "order" di Firestore
        CollectionReference orderRef = db.collection("order");

        // Query untuk mencari pesanan dengan ID yang sesuai
        orderRef.whereEqualTo("orderId", orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Jika ditemukan pesanan dengan ID yang sama, kembalikan true
                        boolean isOrderSaved = !task.getResult().isEmpty();
                        if (isOrderSaved) {
                            Toast.makeText(CartPage.this, "Pesanan sudah pernah disimpan sebelumnya!", Toast.LENGTH_SHORT).show();
                        }
                        callback.onCallback(isOrderSaved);
                    } else {
                        // Tangani kesalahan jika ada
                        Toast.makeText(CartPage.this, "Gagal memeriksa pesanan!", Toast.LENGTH_SHORT).show();
                        callback.onCallback(false);
                    }
                });
    }

    // Metode untuk menghasilkan ID pesanan otomatis berdasarkan timestamp saat ini
    private String generateAutomaticOrderId() {
        // Gunakan timestamp saat ini sebagai bagian dari ID pesanan
        long timestamp = System.currentTimeMillis();

        // Anda dapat menambahkan komponen tambahan untuk membuat ID lebih unik jika diperlukan
        // Misalnya, Anda mungkin menambahkan ID pengguna atau beberapa karakter acak
        String uniqueComponent = ""; // Ganti dengan logika Anda

        return "ORDER_" + timestamp + "_" + uniqueComponent;
    }


    // Interface untuk callback hasil
    interface ResultCallback<T> {
        void onCallback(T result);
    }

    private void clearOrderFromFirestore() {
        // Mendapatkan referensi ke koleksi "pesanan" di Firestore
        CollectionReference pesananRef = db.collection("pesanan");

        // Iterasi melalui setiap dokumen dan menghapusnya
        pesananRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Hapus setiap dokumen di koleksi "pesanan"
                for (QueryDocumentSnapshot document : task.getResult()) {
                    pesananRef.document(document.getId()).delete();
                }

                // Setelah menghapus semua pesanan, bersihkan rec_record dan refresh tampilan
                cartItemList.clear();
                cartAdapter.notifyDataSetChanged();

                // Berikan pesan bahwa pesanan telah dihapus
                Toast.makeText(CartPage.this, "Pesanan dihapus!", Toast.LENGTH_SHORT).show();
            } else {
                // Tangani kesalahan jika gagal menghapus pesanan
                Toast.makeText(CartPage.this, "Gagal menghapus pesanan!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
