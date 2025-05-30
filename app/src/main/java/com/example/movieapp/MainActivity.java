package com.example.movieapp;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.movieapp.Adapter.MovieAdapter;
import com.example.movieapp.Adapter.NowPlayingAdapter;
import com.example.movieapp.Movie;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 nowPlayingViewPager;
    private RecyclerView bestMoviesRecyclerView;
    private RelativeLayout notificationPanel;
    private boolean isPanelShown = false;
    private Handler sliderHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        // Gọi hàm lấy dữ liệu phim từ Firebase
        loadMoviesFromFirebase();
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
        int id = view.getId();

        if (id == R.id.btn_home) {
            // Xử lý khi click Home
        } else if (id == R.id.btn_ticket) {
            // Xử lý khi click Ticket
        } else if (id == R.id.btn_movie) {
            // Xử lý khi click Movie
        } else if (id == R.id.btn_account) {
            // Xử lý khi click Account
        }
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

                    movieList.add(new Movie(title, imageUrl, movieId));
                }

                setupNowPlayingViewPager(movieList);
                setupBestMoviesRecyclerView(movieList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNowPlayingViewPager(List<Movie> movies) {
        NowPlayingAdapter adapter = new NowPlayingAdapter(movies, this);
        nowPlayingViewPager.setAdapter(adapter);
    }

    private void setupBestMoviesRecyclerView(List<Movie> movies) {
        MovieAdapter adapter = new MovieAdapter(movies, this);
        bestMoviesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bestMoviesRecyclerView.setAdapter(adapter);
    }
}
