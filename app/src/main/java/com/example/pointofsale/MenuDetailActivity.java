package com.example.pointofsale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pointofsale.model.Pesanan;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MenuDetailActivity extends AppCompatActivity {

    // Deklarasi EditText
    EditText edtJumlah;
    TextView txtNamaMenuDetail;
    TextView txtHargaMenuDetail;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        dbRef = FirebaseDatabase.getInstance().getReference("pesanan");
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

        // Membuat objek pesanan
        Pesanan pesanan = new Pesanan(menu, harga, kuantitas);

        // Menyimpan data ke Realtime Database
        dbRef.push().setValue(pesanan)
                .addOnSuccessListener(aVoid -> {
                    // Data berhasil disimpan
                    // Tambahkan logika atau pindah ke aktivitas lain jika diperlukan
                    startActivity(new Intent(MenuDetailActivity.this, CartPage.class));
                })
                .addOnFailureListener(e -> {
                    // Gagal menyimpan data
                    e.printStackTrace();
                    // Tambahkan penanganan kesalahan sesuai kebutuhan
                });
    }

    public void cancelPesanan() {
        // Implement the cancel order logic here
        // For example, you can finish the current activity
        finish();
    }
}
