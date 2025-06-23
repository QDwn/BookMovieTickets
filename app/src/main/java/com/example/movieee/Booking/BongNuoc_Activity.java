package com.example.movieee.Booking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieee.Adapter.ComboAdapter;
import com.example.movieee.Model.ComboBN;
import com.example.movieee.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BongNuoc_Activity extends AppCompatActivity implements ComboAdapter.OnQuantityChangeListener {

    private RecyclerView recyclerCombo;
    private TextView txtTongTien;
    private Button btnThanhToan;
    private ComboAdapter comboAdapter;
    private List<ComboBN> comboList = new ArrayList<>();

    private String movieId, ngay, diaDiem, gio;
    private String movieTitle; // Thêm biến để nhận tên phim
    private ArrayList<String> selectedSeats;
    private int seatTotalPrice; // Để lưu tổng giá tiền ghế

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bong_nuoc);

        recyclerCombo = findViewById(R.id.recycler_combo);
        txtTongTien = findViewById(R.id.txt_tong_tien);
        btnThanhToan = findViewById(R.id.btn_thanh_toan);

        // Lấy dữ liệu được truyền từ DatGhe_Activity
        Intent intent = getIntent();
        if (intent != null) {
            movieId = intent.getStringExtra("movieId");
            movieTitle = intent.getStringExtra("movieTitle"); // Nhận tên phim
            ngay = intent.getStringExtra("ngay");
            diaDiem = intent.getStringExtra("diaDiem");
            gio = intent.getStringExtra("gio");
            selectedSeats = intent.getStringArrayListExtra("selectedSeats");
            seatTotalPrice = intent.getIntExtra("seatTotalPrice", 0);
        }

        comboAdapter = new ComboAdapter(comboList, this, this);
        recyclerCombo.setLayoutManager(new LinearLayoutManager(this));
        recyclerCombo.setAdapter(comboAdapter);

        loadCombosFromFirebase();
        updateTotalPrice();

        btnThanhToan.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(this, ThanhToan_Activity.class);
            paymentIntent.putExtra("movieId", movieId);
            paymentIntent.putExtra("movieTitle", movieTitle); // Truyền tên phim
            paymentIntent.putExtra("ngay", ngay);
            paymentIntent.putExtra("diaDiem", diaDiem);
            paymentIntent.putExtra("gio", gio);
            paymentIntent.putStringArrayListExtra("selectedSeats", selectedSeats);

            int comboTotalPrice = 0;
            for (ComboBN c : comboList) {
                comboTotalPrice += c.getPrice() * c.getQuantity();
            }
            paymentIntent.putExtra("comboTotalPrice", comboTotalPrice);
            paymentIntent.putExtra("seatTotalPrice", seatTotalPrice);

            startActivity(paymentIntent);
        });
    }

    private void loadCombosFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("bong_nuoc");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ComboBN combo = snap.getValue(ComboBN.class);
                    if (combo != null) {
                        comboList.add(combo);
                    }
                }
                comboAdapter.notifyDataSetChanged();
                updateTotalPrice();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BongNuoc_Activity.this, "Lỗi tải dữ liệu combo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onQuantityChanged() {
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        int comboTotal = 0;
        for (ComboBN c : comboList) {
            comboTotal += c.getPrice() * c.getQuantity();
        }
        int total = seatTotalPrice + comboTotal;

        String formatted = NumberFormat.getInstance(new Locale("vi", "VN")).format(total);
        txtTongTien.setText("Tổng tiền: " + formatted + "đ");
    }
}