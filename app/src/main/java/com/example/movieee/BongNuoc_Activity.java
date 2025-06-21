package com.example.movieee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieee.Adapter.ComboAdapter;
import com.example.movieee.Model.ComboBN;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bong_nuoc);

        recyclerCombo = findViewById(R.id.recycler_combo);
        txtTongTien = findViewById(R.id.txt_tong_tien);
        btnThanhToan = findViewById(R.id.btn_thanh_toan);

        comboAdapter = new ComboAdapter(comboList, this, this);
        recyclerCombo.setLayoutManager(new LinearLayoutManager(this));
        recyclerCombo.setAdapter(comboAdapter);

        loadCombosFromFirebase();

        btnThanhToan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThanhToan_Activity.class);
            startActivity(intent);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BongNuoc_Activity.this, "Lỗi tải dữ liệu combo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onQuantityChanged() {
        int tong = 0;
        for (ComboBN c : comboList) {
            tong += c.getPrice() * c.getQuantity();
        }
        String formatted = NumberFormat.getInstance(new Locale("vi", "VN")).format(tong);
        txtTongTien.setText("Tổng tiền: " + formatted + "đ");
    }
}
