package com.example.movieee;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

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

        // --- Lấy link ảnh từ danh_sach_phim ---
        DatabaseReference posterRef = FirebaseDatabase.getInstance()
                .getReference("danh_sach_phim") // Lấy từ danh_sach_phim
                .child(movieId);

        posterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot posterSnapshot) {
                String imageUrl = posterSnapshot.child("poster").getValue(String.class); // Lấy URL poster
                // (Optional) If you want to display the poster here, add an ImageView in layout
                // and use Glide.with(ChitietMovie_Activity.this).load(imageUrl).into(yourImageView);

                // --- Lấy thông tin chi tiết từ chi_tiet_phim ---
                DatabaseReference detailRef = FirebaseDatabase.getInstance()
                        .getReference("chi_tiet_phim") // Lấy từ chi_tiet_phim
                        .child(movieId);

                detailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot detailSnapshot) {
                        if (detailSnapshot.exists()) {
                            String tenPhim = detailSnapshot.child("ten_phim").getValue(String.class);
                            String moTa = detailSnapshot.child("mo_ta").getValue(String.class);
                            String khoiChieu = detailSnapshot.child("khoi_chieu").getValue(String.class);
                            String thoiluongVal = detailSnapshot.child("thoi_luong").getValue(String.class);
                            String daoDien = detailSnapshot.child("dao_dien").getValue(String.class);
                            Double ratingValue = detailSnapshot.child("danh_gia").getValue(Double.class);
                            String danhGia = (ratingValue != null) ? String.valueOf(ratingValue) : "Chưa đánh giá";
                            String trailerUrl = detailSnapshot.child("trailer").getValue(String.class);

                            // Diễn viên dạng list
                            GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                            List<String> castList = detailSnapshot.child("dien_vien").getValue(t);
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
                            Toast.makeText(ChitietMovie_Activity.this, "Không tìm thấy chi tiết phim.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChitietMovie_Activity.this, "Lỗi khi tải chi tiết phim: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChitietMovie_Activity.this, "Lỗi khi tải poster phim: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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