package com.example.movieee;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.movieee.Adapter.HomeMovieAdapter;
import com.example.movieee.Adapter.NowPlayingAdapter;
import com.example.movieee.Model.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    private ViewPager2 nowPlayingViewPager;
    private RecyclerView bestMoviesRecyclerView;
    private RelativeLayout notificationPanel;
    private boolean isPanelShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nowPlayingViewPager = findViewById(R.id.now_playing_view_pager);
        bestMoviesRecyclerView = findViewById(R.id.view1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        notificationPanel = findViewById(R.id.notification_panel);

        ImageButton bellButton = findViewById(R.id.bell_icon);
        bellButton.setOnClickListener(v -> toggleNotificationPanel());

        loadMoviesFromFirebase(); // 🔥 Lấy dữ liệu và hiển thị poster + tên phim
    }

    private void toggleNotificationPanel() {
        if (isPanelShown) {
            notificationPanel.animate()
                    .translationX(notificationPanel.getWidth())
                    .setDuration(300)
                    .withEndAction(() -> notificationPanel.setVisibility(View.GONE));
        } else {
            notificationPanel.setVisibility(View.VISIBLE);
            notificationPanel.setTranslationX(notificationPanel.getWidth());
            notificationPanel.animate()
                    .translationX(0)
                    .setDuration(300);
        }
        isPanelShown = !isPanelShown;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isPanelShown) {
            Rect viewRect = new Rect();
            notificationPanel.getGlobalVisibleRect(viewRect);
            if (!viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                toggleNotificationPanel();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onMenuButtonClick(View view) {
        // Xử lý nút menu nếu cần
    }

    private void loadMoviesFromFirebase() {
        DatabaseReference movieListRef = FirebaseDatabase.getInstance().getReference("danh_sach_phim");

        movieListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Movie> movieList = new ArrayList<>();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    String movieId = movieSnapshot.getKey();
                    String title = movieSnapshot.child("ten_phim").getValue(String.class);
                    String imageUrl = movieSnapshot.child("poster").getValue(String.class);

                    if (title != null && imageUrl != null) {
                        movieList.add(new Movie(title, imageUrl, movieId));
                    }
                }

                // Dùng chung adapter để hiển thị hình + tiêu đề
                setupBestMoviesRecyclerView(movieList);
                setupNowPlayingViewPager(nowPlayingMovies);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity2.this, "Lỗi tải dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }
    List<Movie> nowPlayingMovies = List.of(
            new Movie("Movie 1", R.drawable.quydinh),
            new Movie("Movie 2", R.drawable.rapphim),
            new Movie("Movie 2", R.drawable.rapphim1)
    );
    private void setupNowPlayingViewPager(List<Movie> movies) {
        NowPlayingAdapter adapter = new NowPlayingAdapter(movies, this);
        nowPlayingViewPager.setAdapter(adapter);
    }

    private void setupBestMoviesRecyclerView(List<Movie> movies) {
        HomeMovieAdapter adapter = new HomeMovieAdapter(movies, this);
        bestMoviesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bestMoviesRecyclerView.setAdapter(adapter);
    }
}
