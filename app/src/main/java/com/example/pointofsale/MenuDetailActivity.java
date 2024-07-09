package com.example.pointofsale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pointofsale.model.Pesanan;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MenuDetailActivity extends AppCompatActivity {

    // Deklarasi EditText
    EditText edtJumlah;
    TextView txtNamaMenuDetail;
    TextView txtHargaMenuDetail;
    private DatabaseReference dbRef;
    private DatabaseReference pesananRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        dbRef = FirebaseDatabase.getInstance().getReference();
        pesananRef = dbRef.child("pesanan");

        txtNamaMenuDetail = findViewById(R.id.txtNamaMenuDetail);
        txtHargaMenuDetail = findViewById(R.id.txtHargaMenuDetail);
        edtJumlah = findViewById(R.id.edt_jumlah);

        // Retrieve data from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("name");
            String price = extras.getString("price");

            // Update TextViews
            txtNamaMenuDetail.setText(name);
            txtHargaMenuDetail.setText(price);
        }

        // Inisialisasi EditText
        edtJumlah = findViewById(R.id.edt_jumlah);

        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPesanan();
            }
        });
    }

    public void postPesanan(View view) {
        // Mendapatkan nilai dari EditText
        String menu = txtNamaMenuDetail.getText().toString();
        String hargaString = txtHargaMenuDetail.getText().toString();
        String kuantitasString = edtJumlah.getText().toString();

        // Mengonversi nilai harga dari string ke integer
        int harga = 0;
        try {
            harga = Integer.parseInt(hargaString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // Handle the case where hargaString is not a valid integer
            // You may want to show an error message to the user or take appropriate action
            return;
        }

        // Mengonversi nilai kuantitas dari string ke integer
        int kuantitas = 0;
        try {
            kuantitas = Integer.parseInt(kuantitasString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // Handle the case where kuantitasString is not a valid integer
            // You may want to show an error message to the user or take appropriate action
            return;
        }

        // Periksa stok yang tersedia sebelum menambahkan pesanan
        checkStockAndAddToCart(menu, harga, kuantitas);
    }

    private void checkStockAndAddToCart(String menu, int harga, int kuantitas) {
        Query foodQuery = FirebaseDatabase.getInstance().getReference("menu/food").orderByChild("name").equalTo(menu);

        // Cek stok untuk food
        foodQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
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

                        if (currentStock != null && currentStock >= kuantitas) {
                            // Memeriksa apakah item sudah ada di keranjang
                            isItemInCart(menu, harga, kuantitas);
                        } else {
                            // Tampilkan pesan bahwa stok tidak mencukupi
                            Toast.makeText(MenuDetailActivity.this, "Stok tidak mencukupi", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Cek stok untuk drink jika tidak ada di food
                    checkDrinkStockAndAddToCart(menu, harga, kuantitas);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                error.toException().printStackTrace();
            }
        });
    }

    private void checkDrinkStockAndAddToCart(String menu, int harga, int kuantitas) {
        Query drinkQuery = FirebaseDatabase.getInstance().getReference("menu/drink").orderByChild("name").equalTo(menu);

        drinkQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
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

                        if (currentStock != null && currentStock >= kuantitas) {
                            // Memeriksa apakah item sudah ada di keranjang
                            isItemInCart(menu, harga, kuantitas);
                        } else {
                            // Tampilkan pesan bahwa stok tidak mencukupi
                            Toast.makeText(MenuDetailActivity.this, "Stok tidak mencukupi", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Tampilkan pesan bahwa item tidak ditemukan
                    Toast.makeText(MenuDetailActivity.this, "Item tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                error.toException().printStackTrace();
            }
        });
    }

    private void isItemInCart(String menu, int harga, int kuantitas) {
        pesananRef.orderByChild("menu").equalTo(menu).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Item belum ada di keranjang, tambahkan ke database
                    Pesanan pesanan = new Pesanan(menu, harga, kuantitas);
                    pesananRef.push().setValue(pesanan)
                            .addOnSuccessListener(aVoid -> {
                                // Data berhasil disimpan
                                startActivity(new Intent(MenuDetailActivity.this, CartPage.class));
                            })
                            .addOnFailureListener(e -> {
                                // Gagal menyimpan data
                                e.printStackTrace();
                                Toast.makeText(MenuDetailActivity.this, "Gagal menambahkan pesanan", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Item sudah ada dalam keranjang
                    Toast.makeText(MenuDetailActivity.this, "Item sudah ada dalam keranjang", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                error.toException().printStackTrace();
                Toast.makeText(MenuDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cancelPesanan() {
        // Implement the cancel order logic here
        finish();
    }
}
