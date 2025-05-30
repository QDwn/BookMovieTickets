package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ChitietMovie_Activity extends AppCompatActivity {

    private ShapeableImageView moviePosterDetail;
    private TextView movieTitleDetail, movieDescription, movieDuration, movieReleaseDate;
    private Button btnBookTicket;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chitiet_movie);

        // Ánh xạ các view
        moviePosterDetail = findViewById(R.id.movie_poster_detail);
        movieTitleDetail = findViewById(R.id.movie_title_detail);
        movieDescription = findViewById(R.id.movie_description);
        movieDuration = findViewById(R.id.movie_duration);
        movieReleaseDate = findViewById(R.id.movie_release_date);
        btnBookTicket = findViewById(R.id.btn_book_ticket);
        btnBack = findViewById(R.id.btn_back);

        // Nhận movieId từ Intent
        String movieId = getIntent().getStringExtra("movieId");

        if (movieId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Truy vấn Firebase
        DatabaseReference movieRef = FirebaseDatabase.getInstance()
                .getReference("chi_tiet_phim")
                .child(movieId);

        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    String duration = snapshot.child("duration").getValue(String.class);
                    String releaseDate = snapshot.child("releaseDate").getValue(String.class);

                    movieTitleDetail.setText(title);
                    movieDescription.setText(description);
                    movieDuration.setText("Thời lượng: " + duration);
                    movieReleaseDate.setText("Ngày chiếu: " + releaseDate);

                    Glide.with(ChitietMovie_Activity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_poster)
                            .into(moviePosterDetail);
                } else {
                    Toast.makeText(ChitietMovie_Activity.this, "Phim không tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChitietMovie_Activity.this, "Lỗi tải dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        btnBookTicket.setOnClickListener(v -> {
            Intent intent = new Intent(this, Datve_Activity.class);
            startActivity(intent);
        });
    }
}
