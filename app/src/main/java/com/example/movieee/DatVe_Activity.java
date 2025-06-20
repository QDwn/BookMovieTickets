package com.example.movieee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatVe_Activity extends AppCompatActivity {

    private ImageView posterImage;
    private TextView txtTitle, txtCumRap, txtThongBao;
    private Button btnCauGiay, btnDongDa, btnTiep;
    private LinearLayout dateContainer;
    private GridLayout showtimeGrid;

    private String movieId;
    private String selectedDate = "";
    private String selectedLocation = "Cầu Giấy";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dat_ve);

        // Ánh xạ view
        posterImage = findViewById(R.id.poster_image);
        txtTitle = findViewById(R.id.txt_title);
        txtCumRap = findViewById(R.id.txt_cum_rap);
        txtThongBao = findViewById(R.id.txt_thong_bao);
        btnCauGiay = findViewById(R.id.btn_caugiay);
        btnDongDa = findViewById(R.id.btn_dongda);
        btnTiep = findViewById(R.id.btn_tiep);
        dateContainer = findViewById(R.id.date_container);
        showtimeGrid = findViewById(R.id.showtime_grid);

        // Nhận movieId
        movieId = getIntent().getStringExtra("movieId");
        if (movieId == null || movieId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMovieInfo();
        setupLocationButtons();
        generateDateButtons();
        setupButtonTiep();
    }

    private void loadMovieInfo() {
        DatabaseReference movieRef = FirebaseDatabase.getInstance().getReference("danh_sach_phim").child(movieId);
        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String title = snapshot.child("ten_phim").getValue(String.class);
                String imageUrl = snapshot.child("poster").getValue(String.class);
                txtTitle.setText(title);
                Glide.with(DatVe_Activity.this).load(imageUrl).into(posterImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DatVe_Activity.this, "Lỗi tải thông tin phim", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateDateButtons() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 3; i++) {
            String dateStr = sdf.format(calendar.getTime());
            Button btn = new Button(this);
            btn.setText(dateStr);
            btn.setOnClickListener(v -> {
                selectedDate = dateStr;
                loadShowtimes();
            });
            dateContainer.addView(btn);
            if (i == 0) selectedDate = dateStr;
            calendar.add(Calendar.DATE, 1);
        }
        loadShowtimes();
    }

    private void setupLocationButtons() {
        btnCauGiay.setOnClickListener(v -> {
            selectedLocation = "Cầu Giấy";
            loadShowtimes();
        });

        btnDongDa.setOnClickListener(v -> {
            selectedLocation = "Đống Đa";
            loadShowtimes();
        });
    }

    private void loadShowtimes() {
        showtimeGrid.removeAllViews();
        txtThongBao.setText("");
        btnTiep.setVisibility(View.GONE);

        txtCumRap.setText("Rạp: " + selectedLocation);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("lich_chieu_phim")
                .child("lich_chieu")
                .child(movieId)
                .child(selectedDate)
                .child(selectedLocation);

        Log.d("FIREBASE_PATH", "movieId=" + movieId + ", date=" + selectedDate + ", location=" + selectedLocation);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    txtThongBao.setText("Không có suất chiếu cho ngày này");
                    return;
                }
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String gio = snap.getValue(String.class);
                    if (gio == null) continue;
                    Button btnGio = new Button(DatVe_Activity.this);
                    btnGio.setText(gio);
                    btnGio.setOnClickListener(v -> {
                        selectedTime = gio;
                        txtThongBao.setText("\uD83D\uDD50 Bạn có 10 phút để đặt vé!");
                        btnTiep.setVisibility(View.VISIBLE);
                    });
                    showtimeGrid.addView(btnGio);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DatVe_Activity.this, "Lỗi tải giờ chiếu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonTiep() {
        btnTiep.setOnClickListener(v -> {
            if (selectedTime.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn giờ chiếu trước khi tiếp tục", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, DatGhe_Activity.class);
            intent.putExtra("movieId", movieId);
            intent.putExtra("ngay", selectedDate);
            intent.putExtra("diaDiem", selectedLocation);
            intent.putExtra("gio", selectedTime);
            startActivity(intent);
        });
    }
}
