package com.example.movieee;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.util.Log;

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
import com.example.movieee.Adapter.RankingAdapter;
import com.example.movieee.Model.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class MainActivity2 extends AppCompatActivity {
    private ViewPager2 nowPlayingViewPager;
    private RecyclerView bestMoviesRecyclerView;
    private RecyclerView verticalRecyclerView;
    private RelativeLayout notificationPanel;
    private boolean isPanelShown = false;

    private Button btnHanhDong, btnHoatHinh, btnChinhKich, btnPhieuLuu, btnKinhDi;
    private FirebaseAuth mAuth;
    private List<Movie> nowPlayingMovies;

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
        verticalRecyclerView = findViewById(R.id.recycler_vertical);
        notificationPanel = findViewById(R.id.notification_panel);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton bellButton = findViewById(R.id.bell_icon);
        bellButton.setOnClickListener(v -> toggleNotificationPanel());

        // Init buttons
        btnHanhDong = findViewById(R.id.btnHanhDong);
        btnHoatHinh = findViewById(R.id.btnHoatHinh);
        btnChinhKich = findViewById(R.id.btnChinhKich);
        btnPhieuLuu = findViewById(R.id.btnPhieuLuu);
        btnKinhDi = findViewById(R.id.btnKinhDi);

        btnHanhDong.setOnClickListener(v -> openTheLoai("Hành động"));
        btnHoatHinh.setOnClickListener(v -> openTheLoai("Hoạt hình"));
        btnChinhKich.setOnClickListener(v -> openTheLoai("Chính kịch"));
        btnPhieuLuu.setOnClickListener(v -> openTheLoai("Phiêu lưu"));
        btnKinhDi.setOnClickListener(v -> openTheLoai("Kinh dị"));

        mAuth = FirebaseAuth.getInstance();

        loadMoviesFromFirebase();
        loadVerticalMoviesFromFirebase();
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
        if (id == R.id.btn_account) {
            String currentLoggedInUserEmail = mAuth.getCurrentUser() != null ?
                    mAuth.getCurrentUser().getEmail() : null;

            Log.d("AccountDebug", "Email hiện tại: " + currentLoggedInUserEmail);
            Toast.makeText(this, "Email: " + (currentLoggedInUserEmail != null ? currentLoggedInUserEmail : "NULL"), Toast.LENGTH_SHORT).show();

            if (currentLoggedInUserEmail != null) {
                Intent intent = new Intent(MainActivity2.this, AccountDetailsActivity.class);
                intent.putExtra("userEmail", currentLoggedInUserEmail);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Bạn chưa đăng nhập.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity2.this, LoginActivity.class));
            }
        } else if (id == R.id.btn_home) {
            Toast.makeText(this, "Bạn đang ở Trang chủ", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btn_ticket) {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(MainActivity2.this, UserTicketsActivity.class));
            } else {
                Toast.makeText(this, "Bạn cần đăng nhập để xem vé đã đặt.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity2.this, LoginActivity.class));
            }
        } else if (id == R.id.btn_movie) {
            Toast.makeText(this, "Chức năng Phim đang được phát triển", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMoviesFromFirebase() {
        nowPlayingMovies = List.of(
                new Movie("Movie 1", R.drawable.quydinh, "np_001"),
                new Movie("Movie 2", R.drawable.rapphim, "np_002"),
                new Movie("Movie 3", R.drawable.rapphim1, "np_003")
        );
        setupNowPlayingViewPager(nowPlayingMovies);

        DatabaseReference movieListRef = FirebaseDatabase.getInstance().getReference("danh_sach_phim");
        movieListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Movie> bestMovieList = new ArrayList<>();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    String movieId = movieSnapshot.getKey();
                    String title = movieSnapshot.child("ten_phim").getValue(String.class);
                    String imageUrl = movieSnapshot.child("poster").getValue(String.class);
                    if (title != null && imageUrl != null && movieId != null) {
                        bestMovieList.add(new Movie(title, imageUrl, movieId));
                    }
                }
                setupBestMoviesRecyclerView(bestMovieList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity2.this, "Lỗi tải danh sách phim", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVerticalMoviesFromFirebase() {
        DatabaseReference movieDetailRef = FirebaseDatabase.getInstance().getReference("chi_tiet_phim");

        movieDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Movie> verticalList = new ArrayList<>();

                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    String movieId = movieSnapshot.getKey();
                    if (movieId == null || !movieId.matches("id_phim(1[1-9]|[2-9][0-9]|[1-9][0-9]{2,})")) continue;

                    String title = movieSnapshot.child("ten_phim").getValue(String.class);
                    String poster = movieSnapshot.child("imageUrl").getValue(String.class);
                    String duration = movieSnapshot.child("thoi_luong").getValue(String.class);
                    Double rating = movieSnapshot.child("danh_gia").getValue(Double.class);

                    Movie movie = new Movie(title, poster, movieId);
                    movie.setDuration(duration);
                    movie.setRating(rating);
                    verticalList.add(movie);
                }

                verticalList.sort(Comparator.comparing((Movie m) -> m.getRating() != null ? m.getRating() : 0.0).reversed());

                RankingAdapter adapter = new RankingAdapter(verticalList, MainActivity2.this);
                verticalRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity2.this));
                verticalRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity2.this, "Lỗi tải phim đánh giá cao", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNowPlayingViewPager(List<Movie> movies) {
        NowPlayingAdapter adapter = new NowPlayingAdapter(movies, this);
        nowPlayingViewPager.setAdapter(adapter);
    }

    private void setupBestMoviesRecyclerView(List<Movie> movies) {
        HomeMovieAdapter adapter = new HomeMovieAdapter(movies, this);
        bestMoviesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bestMoviesRecyclerView.setAdapter(adapter);
    }

    private void openTheLoai(String theLoai) {
        Intent intent = new Intent(MainActivity2.this, TheLoai_Activity.class);
        intent.putExtra("genre", theLoai);
        startActivity(intent);
    }
}
