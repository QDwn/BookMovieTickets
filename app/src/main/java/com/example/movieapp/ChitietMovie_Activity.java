package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ChitietMovie_Activity extends AppCompatActivity {

    private WebView trailerWebView;
    private TextView titleDetail, description, releaseDate, director, cast, rating, thoiluong;
    private Button bookTicket;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chitiet_movie);

        // Ánh xạ view
        back = findViewById(R.id.back);
        trailerWebView = findViewById(R.id.trailer_video);
        titleDetail = findViewById(R.id.title_detail);
        description = findViewById(R.id.description);
        releaseDate = findViewById(R.id.release_date);
        thoiluong = findViewById(R.id.thoiluong);
        director = findViewById(R.id.director);
        cast = findViewById(R.id.cast);
        rating = findViewById(R.id.rating);
        bookTicket = findViewById(R.id.book_ticket);

        // Nhận movieId từ intent
        String movieId = getIntent().getStringExtra("movieId");
        if (movieId == null || movieId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Truy vấn dữ liệu từ Firebase
        DatabaseReference movieRef = FirebaseDatabase.getInstance()
                .getReference("chi_tiet_phim")
                .child(movieId);

        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String tenPhim = snapshot.child("ten_phim").getValue(String.class);
                    String moTa = snapshot.child("mo_ta").getValue(String.class);
                    String khoiChieu = snapshot.child("khoi_chieu").getValue(String.class);
                    String thoiluongVal = snapshot.child("thoi_luong").getValue(String.class);
                    String daoDien = snapshot.child("dao_dien").getValue(String.class);
                    Double ratingValue = snapshot.child("danh_gia").getValue(Double.class);
                    String danhGia = (ratingValue != null) ? String.valueOf(ratingValue) : "Chưa đánh giá";
                    String trailerUrl = snapshot.child("trailer").getValue(String.class);

                    // Diễn viên dạng list
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                    List<String> castList = snapshot.child("dien_vien").getValue(t);
                    String dienVien = (castList != null) ? TextUtils.join(", ", castList) : "Chưa rõ";

                    if (tenPhim != null) titleDetail.setText(tenPhim);
                    if (moTa != null) description.setText(moTa);
                    if (khoiChieu != null) releaseDate.setText("Ngày chiếu: " + khoiChieu);
                    if (thoiluongVal != null) thoiluong.setText("Thời lượng: " + thoiluongVal);
                    if (daoDien != null) director.setText("Đạo diễn: " + daoDien);
                    cast.setText("Diễn viên: " + dienVien);
                    rating.setText("Đánh giá: " + danhGia + " ★");

                    if (trailerUrl != null && !trailerUrl.isEmpty()) {
                        String html = "<html><body style='margin:0;padding:0;'><iframe width=\"100%\" height=\"100%\" " +
                                "src=\"" + trailerUrl + "\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
                        WebSettings webSettings = trailerWebView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        trailerWebView.loadData(html, "text/html", "utf-8");
                    } else {
                        trailerWebView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ChitietMovie_Activity.this, "Phim không tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChitietMovie_Activity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        back.setOnClickListener(v -> finish());

        bookTicket.setOnClickListener(v -> {
            Intent intent = new Intent(this, Datve_Activity.class);
            startActivity(intent);
        });
    }
}
