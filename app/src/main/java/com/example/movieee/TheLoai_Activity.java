package com.example.movieee;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieee.Adapter.HomeMovieAdapter;
import com.example.movieee.Model.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TheLoai_Activity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvTheLoaiTitle;
    HomeMovieAdapter adapter;
    List<Movie> movieList;
    String theLoai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_the_loai);

        recyclerView = findViewById(R.id.recyclerViewTheLoai);
        tvTheLoaiTitle = findViewById(R.id.tvTheLoaiTitle);

        // Nhận thể loại từ Intent
        theLoai = getIntent().getStringExtra("theloai");
        tvTheLoaiTitle.setText("Thể loại: " + theLoai);

        // Khởi tạo list và adapter
        movieList = new ArrayList<>();
        adapter = new HomeMovieAdapter(movieList, this, R.layout.item_the_loai_movie);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Tải phim theo thể loại
        loadMoviesTheoTheLoai(theLoai);
    }

    private void loadMoviesTheoTheLoai(String theLoai) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chi_tiet_phim");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                movieList.clear(); // clear list trước khi thêm mới
                for (DataSnapshot movieSnap : snapshot.getChildren()) {
                    String id = movieSnap.getKey();
                    String title = movieSnap.child("ten_phim").getValue(String.class);
                    String poster = movieSnap.child("imageUrl").getValue(String.class);

                    // Lấy danh sách thể loại (List<String>)
                    List<String> dsTheLoai = new ArrayList<>();
                    for (DataSnapshot genreSnap : movieSnap.child("the_loai").getChildren()) {
                        String tl = genreSnap.getValue(String.class);
                        if (tl != null) dsTheLoai.add(tl);
                    }

                    // Kiểm tra thể loại
                    if (dsTheLoai.contains(theLoai)) {
                        movieList.add(new Movie(title, poster, id));
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TheLoai_Activity.this, "Lỗi khi tải thể loại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
