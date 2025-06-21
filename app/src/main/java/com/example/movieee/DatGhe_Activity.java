package com.example.movieee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieee.Adapter.SeatAdapter;
import com.example.movieee.Model.Seat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import java.text.NumberFormat;
import java.util.Locale;

public class DatGhe_Activity extends AppCompatActivity implements SeatAdapter.OnSeatSelectedListener {

    private RecyclerView recyclerGhe;
    private TextView txtGheDaChon;
    private Button btnXacNhan;
    private TextView txtTongTien;
    private static final int GIA_GHE = 50000;


    private String movieId, ngay, diaDiem, gio;
    private List<Seat> seatList = new ArrayList<>();
    private SeatAdapter seatAdapter;

    // Biến để lưu tổng tiền ghế
    private int currentSeatTotalPrice = 0; // Thêm biến này

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dat_ghe);

        recyclerGhe = findViewById(R.id.recycler_ghe);
        txtGheDaChon = findViewById(R.id.txt_ghe_da_chon);
        btnXacNhan = findViewById(R.id.btn_xac_nhan);
        txtTongTien = findViewById(R.id.txt_tong_tien);


        movieId = getIntent().getStringExtra("movieId");
        ngay = getIntent().getStringExtra("ngay");
        diaDiem = getIntent().getStringExtra("diaDiem");
        gio = getIntent().getStringExtra("gio");

        generateSeatList();

        recyclerGhe.setLayoutManager(new GridLayoutManager(this, 8));
        seatAdapter = new SeatAdapter(seatList, this, this);
        recyclerGhe.setAdapter(seatAdapter);

        loadBookedSeatsFromFirebase();

        btnXacNhan.setOnClickListener(v -> {
            List<String> selectedSeats = new ArrayList<>();
            for (Seat s : seatList) {
                if (s.isSelected()) selectedSeats.add(s.getSeatId());
            }
            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Bạn chưa chọn ghế nào!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, BongNuoc_Activity.class);
            intent.putExtra("movieId", movieId);
            intent.putExtra("ngay", ngay);
            intent.putExtra("diaDiem", diaDiem);
            intent.putExtra("gio", gio);
            intent.putStringArrayListExtra("selectedSeats", new ArrayList<>(selectedSeats));
            intent.putExtra("seatTotalPrice", currentSeatTotalPrice); // Truyền tổng tiền ghế đã chọn
            startActivity(intent);
        });
    }

    private void generateSeatList() {
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};
        for (String row : rows) {
            for (int i = 1; i <= 8; i++) {
                boolean isDouble = row.equals("L");
                String type = "Stand";
                if (row.equals("E") || row.equals("F") || row.equals("G") || row.equals("H")) {
                    type = "VIP";
                } else if (isDouble) {
                    type = "Couple";
                }
                seatList.add(new Seat(row + i, false, isDouble, type));
            }
        }
    }

    private void loadBookedSeatsFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("ghe_da_dat")
                .child(movieId)
                .child(ngay)
                .child(gio);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String seatId = snap.getKey();
                    Boolean isBooked = snap.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isBooked)) {
                        for (Seat seat : seatList) {
                            if (seat.getSeatId().equals(seatId)) {
                                seat.setSelected(false);
                                seatList.set(seatList.indexOf(seat), new Seat(seatId, true, seat.isDouble(), seat.getType()));
                                break;
                            }
                        }
                    }
                }
                seatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DatGhe_Activity.this, "Lỗi khi tải ghế đã đặt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSeatSelected(List<String> selectedSeats) {
        txtGheDaChon.setText("Ghế đã chọn: " + String.join(", ", selectedSeats));
        currentSeatTotalPrice = selectedSeats.size() * GIA_GHE; // Cập nhật biến này

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formatted = formatter.format(currentSeatTotalPrice);

        txtTongTien.setText("Tổng tiền: " + formatted + "đ");
    }
}