package com.example.movieee.Home;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieee.Adapter.MovieAdapter;
import com.example.movieee.Model.Movie;
import com.example.movieee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteMoviesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFavoriteMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> favoriteMovieList;
    private TextView tvFavoriteMoviesTitle;

    private FirebaseAuth mAuth;
    private DatabaseReference favoriteMoviesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_movies);

        tvFavoriteMoviesTitle = findViewById(R.id.tvFavoriteMoviesTitle);
        recyclerViewFavoriteMovies = findViewById(R.id.recyclerViewFavoriteMovies);
        recyclerViewFavoriteMovies.setLayoutManager(new LinearLayoutManager(this));

        favoriteMovieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(favoriteMovieList, this); // Tái sử dụng MovieAdapter
        recyclerViewFavoriteMovies.setAdapter(movieAdapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            favoriteMoviesRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId).child("favoriteMovies");
            loadFavoriteMovies();
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để xem phim yêu thích.", Toast.LENGTH_SHORT).show();

            finish();
        }
    }

    private void loadFavoriteMovies() {
        favoriteMoviesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favoriteMovieList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot movieSnapshot : dataSnapshot.getChildren()) {
                        String movieId = movieSnapshot.getKey();
                        String title = movieSnapshot.child("title").getValue(String.class);
                        String imageUrl = movieSnapshot.child("imageUrl").getValue(String.class);

                        if (title != null && imageUrl != null && movieId != null) {
                            favoriteMovieList.add(new Movie(title, imageUrl, movieId));
                        }
                    }
                }
                if (favoriteMovieList.isEmpty()) {
                    tvFavoriteMoviesTitle.setText("Chưa có phim yêu thích nào.");
                } else {
                    tvFavoriteMoviesTitle.setText("Phim yêu thích của bạn");
                }
                movieAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FavoriteMoviesActivity.this, "Lỗi khi tải phim yêu thích: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}