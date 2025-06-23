// app/src/main/java/com/example/movieee/MainActivity2.java
package com.example.movieee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout; // Thêm import này
import android.widget.RelativeLayout;
import android.widget.TextView; // Thêm import này
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import java.util.stream.Collectors;

public class MainActivity2 extends AppCompatActivity {
    private ViewPager2 nowPlayingViewPager;
    private RecyclerView bestMoviesRecyclerView;
    private RecyclerView verticalRecyclerView;
    private RelativeLayout notificationPanel;
    private boolean isPanelShown = false;

    private Button btnHanhDong, btnHoatHinh, btnChinhKich, btnPhieuLuu, btnKinhDi;
    private FirebaseAuth mAuth;
    private List<Movie> nowPlayingMovies;
    private EditText searchEditText;

    private List<Movie> allMovies;
    private HomeMovieAdapter bestMoviesAdapter;
    private RankingAdapter rankingAdapter;


    private RecyclerView recyclerViewSearchResults;
    private HomeMovieAdapter searchResultsAdapter;

    private androidx.constraintlayout.widget.ConstraintLayout nowPlayingContainer;
    private androidx.constraintlayout.widget.ConstraintLayout bestMoviesContainer;
    private LinearLayout rankingContainer;
    private TextView textView7;
    private TextView txt_danh_sach_theo_danh_gia;
    private TextView textView9;
    private TextView textView10;
    private LinearLayout serviceContainer;
    private TextView aaa;
    private LinearLayout categoriesContainer;


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
        searchEditText = findViewById(R.id.editTextText);


        nowPlayingContainer = findViewById(R.id.nowPlayingContainer);
        bestMoviesContainer = findViewById(R.id.bestMoviesContainer);
        rankingContainer = findViewById(R.id.rankingContainer);
        textView7 = findViewById(R.id.textView7);
        txt_danh_sach_theo_danh_gia = findViewById(R.id.txt_danh_sach_theo_danh_gia);
        textView9 = findViewById(R.id.textView9);
        textView10 = findViewById(R.id.textView10);
        serviceContainer = findViewById(R.id.serviceContainer);
        aaa = findViewById(R.id.aaa);
        categoriesContainer = findViewById(R.id.categoriesContainer);


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

        allMovies = new ArrayList<>();

        nowPlayingMovies = new ArrayList<>();
        setupNowPlayingViewPager(nowPlayingMovies);

        bestMoviesAdapter = new HomeMovieAdapter(new ArrayList<>(), this);
        bestMoviesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bestMoviesRecyclerView.setAdapter(bestMoviesAdapter);


        rankingAdapter = new RankingAdapter(new ArrayList<>(), this);
        verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        verticalRecyclerView.setAdapter(rankingAdapter);

        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));

        searchResultsAdapter = new HomeMovieAdapter(new ArrayList<>(), this, R.layout.item_search_result_text);
        recyclerViewSearchResults.setAdapter(searchResultsAdapter);

        loadThongBaoTuFirebase();

        loadMoviesFromFirebase();
        loadVerticalMoviesFromFirebase();


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMovies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
            // Thay đổi logic ở đây để mở FavoriteMoviesActivity
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(MainActivity2.this, FavoriteMoviesActivity.class));
            } else {
                Toast.makeText(this, "Bạn cần đăng nhập để xem phim yêu thích.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity2.this, LoginActivity.class));
            }
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
                allMovies.clear();
                List<Movie> tempBestMovieList = new ArrayList<>();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    String movieId = movieSnapshot.getKey();
                    String title = movieSnapshot.child("ten_phim").getValue(String.class);
                    String imageUrl = movieSnapshot.child("poster").getValue(String.class);
                    if (title != null && imageUrl != null && movieId != null) {
                        Movie movie = new Movie(title, imageUrl, movieId);
                        allMovies.add(movie);
                        tempBestMovieList.add(movie);
                    }
                }

                bestMoviesAdapter.updateList(tempBestMovieList);
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

                rankingAdapter.updateList(verticalList);
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

    private void filterMovies(String query) {
        if (query.isEmpty()) {
            recyclerViewSearchResults.setVisibility(View.GONE);
            nowPlayingContainer.setVisibility(View.VISIBLE);
            bestMoviesContainer.setVisibility(View.VISIBLE);
            rankingContainer.setVisibility(View.VISIBLE);
            textView7.setVisibility(View.VISIBLE);
            txt_danh_sach_theo_danh_gia.setVisibility(View.VISIBLE);
            textView9.setVisibility(View.VISIBLE);
            textView10.setVisibility(View.VISIBLE);
            serviceContainer.setVisibility(View.VISIBLE);
            aaa.setVisibility(View.VISIBLE);
            categoriesContainer.setVisibility(View.VISIBLE);

            bestMoviesAdapter.updateList(allMovies);
        } else {
            recyclerViewSearchResults.setVisibility(View.VISIBLE);
            nowPlayingContainer.setVisibility(View.GONE);
            bestMoviesContainer.setVisibility(View.GONE);
            rankingContainer.setVisibility(View.GONE);
            textView7.setVisibility(View.GONE);
            txt_danh_sach_theo_danh_gia.setVisibility(View.GONE);
            textView9.setVisibility(View.GONE);
            textView10.setVisibility(View.GONE);
            serviceContainer.setVisibility(View.GONE);
            aaa.setVisibility(View.GONE);
            categoriesContainer.setVisibility(View.GONE);


            List<Movie> filteredList = allMovies.stream()
                    .filter(movie -> movie.getTitle().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            searchResultsAdapter.updateList(filteredList); // Cập nhật RecyclerView tìm kiếm
        }
    }

    private void openTheLoai(String theLoai) {
        Intent intent = new Intent(MainActivity2.this, TheLoai_Activity.class);
        intent.putExtra("genre", theLoai);
        startActivity(intent);
    }
    private void loadThongBaoTuFirebase() {
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString("user_email", "guest").replace(".", "_");

        DatabaseReference thongBaoRef = FirebaseDatabase.getInstance()
                .getReference("thong_bao")
                .child(userEmail);

        thongBaoRef.orderByChild("timestamp").limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot tbSnapshot : snapshot.getChildren()) {
                            String noiDung = tbSnapshot.child("noiDung").getValue(String.class);
                            if (noiDung != null) {
                                addNotification(noiDung);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity2.this, "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
                    }
                });

    }
    public void addNotification(String message) {
        LinearLayout notificationContainer = findViewById(R.id.notification_container);

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(16);
        textView.setPadding(8, 8, 8, 8);
        notificationContainer.addView(textView);

        }
    }
