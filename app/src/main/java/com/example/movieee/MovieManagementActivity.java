package com.example.movieee;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieee.Adapter.MovieManagementAdapter;
import com.example.movieee.Model.Movie;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger; // Thêm import này

public class MovieManagementActivity extends AppCompatActivity implements MovieManagementAdapter.OnItemClickListener {

    private RecyclerView moviesRecyclerView;
    private MovieManagementAdapter movieAdapter;
    private List<Movie> movieList;
    private DatabaseReference danhSachPhimRef; // Ref cho danh_sach_phim (poster, title)
    private DatabaseReference chiTietPhimRef;  // Ref cho chi_tiet_phim (full details)
    private Button btnAddMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_management);

        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAddMovie = findViewById(R.id.btnAddMovie);
        danhSachPhimRef = FirebaseDatabase.getInstance().getReference("danh_sach_phim");
        chiTietPhimRef = FirebaseDatabase.getInstance().getReference("chi_tiet_phim");

        movieList = new ArrayList<>();
        movieAdapter = new MovieManagementAdapter(movieList, this);
        moviesRecyclerView.setAdapter(movieAdapter);

        movieAdapter.setOnItemClickListener(this);

        loadMovies();

        btnAddMovie.setOnClickListener(v -> showAddEditMovieDialog(null));
    }

    private void loadMovies() {
        movieList.clear();
        danhSachPhimRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot danhSachSnapshot) {
                // Sử dụng AtomicInteger để theo dõi số lượng chi tiết phim đã tải xong
                AtomicInteger pendingDetailsFetches = new AtomicInteger(0);

                if (!danhSachSnapshot.exists()) {
                    movieAdapter.notifyDataSetChanged();
                    return;
                }

                // First, iterate through danh_sach_phim to get basic movie info (movieId, title, poster)
                for (DataSnapshot movieBriefSnapshot : danhSachSnapshot.getChildren()) {
                    String movieId = movieBriefSnapshot.getKey();
                    String title = movieBriefSnapshot.child("ten_phim").getValue(String.class);
                    String imageUrl = movieBriefSnapshot.child("poster").getValue(String.class);

                    if (movieId != null && title != null && imageUrl != null) {
                        pendingDetailsFetches.incrementAndGet(); // Tăng số lượng chờ tải chi tiết

                        // Then, fetch full details from chi_tiet_phim for each movie
                        chiTietPhimRef.child(movieId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot detailSnapshot) {
                                String description = detailSnapshot.child("mo_ta").getValue(String.class);
                                String trailerUrl = detailSnapshot.child("trailer").getValue(String.class);
                                String releaseDate = detailSnapshot.child("khoi_chieu").getValue(String.class);
                                String duration = detailSnapshot.child("thoi_luong").getValue(String.class);
                                String director = detailSnapshot.child("dao_dien").getValue(String.class);
                                Double rating = detailSnapshot.child("danh_gia").getValue(Double.class);
                                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                                List<String> castList = detailSnapshot.child("dien_vien").getValue(t);

                                Movie movie = new Movie(title, imageUrl, movieId, description, trailerUrl,
                                        releaseDate, duration, director, castList, rating);
                                movieList.add(movie);

                                if (pendingDetailsFetches.decrementAndGet() == 0) {
                                    // All details fetched, update RecyclerView
                                    movieAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MovieManagementActivity.this, "Lỗi tải chi tiết phim: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                if (pendingDetailsFetches.decrementAndGet() == 0) {
                                    movieAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MovieManagementActivity.this, "Lỗi tải danh sách phim cơ bản: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditMovieDialog(Movie movieToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog_RedBlackTheme);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_movie, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etMovieTitle);
        EditText etImageUrl = dialogView.findViewById(R.id.etMoviePosterUrl);
        EditText etDescription = dialogView.findViewById(R.id.etMovieDescription);
        EditText etTrailerUrl = dialogView.findViewById(R.id.etMovieTrailerUrl);
        EditText etReleaseDate = dialogView.findViewById(R.id.etMovieReleaseDate);
        EditText etDuration = dialogView.findViewById(R.id.etMovieDuration);
        EditText etDirector = dialogView.findViewById(R.id.etMovieDirector);
        EditText etCast = dialogView.findViewById(R.id.etMovieCast);
        EditText etRating = dialogView.findViewById(R.id.etMovieRating);

        boolean isEditMode = (movieToEdit != null);
        if (isEditMode) {
            builder.setTitle("Sửa thông tin phim");
            etTitle.setText(movieToEdit.getTitle());
            etImageUrl.setText(movieToEdit.getImageUrl());
            etDescription.setText(movieToEdit.getDescription());
            etTrailerUrl.setText(movieToEdit.getTrailerUrl());
            etReleaseDate.setText(movieToEdit.getReleaseDate());
            etDuration.setText(movieToEdit.getDuration());
            etDirector.setText(movieToEdit.getDirector());
            if (movieToEdit.getCast() != null) {
                etCast.setText(TextUtils.join(", ", movieToEdit.getCast()));
            }
            if (movieToEdit.getRating() != null) {
                etRating.setText(String.format(Locale.getDefault(), "%.1f", movieToEdit.getRating()));
            }
        } else {
            builder.setTitle("Thêm phim mới");
        }

        builder.setPositiveButton(isEditMode ? "Lưu" : "Thêm", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String trailerUrl = etTrailerUrl.getText().toString().trim();
            String releaseDate = etReleaseDate.getText().toString().trim();
            String duration = etDuration.getText().toString().trim();
            String director = etDirector.getText().toString().trim();
            List<String> cast = Arrays.asList(etCast.getText().toString().trim().split(",\\s*"));
            Double rating = 0.0;
            try {
                rating = Double.parseDouble(etRating.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Đánh giá phải là số.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty() || imageUrl.isEmpty() || description.isEmpty() ||
                    releaseDate.isEmpty() || duration.isEmpty() || director.isEmpty() || cast.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đủ thông tin bắt buộc.", Toast.LENGTH_SHORT).show();
                return;
            }

            String targetMovieId;
            if (isEditMode) {
                targetMovieId = movieToEdit.getMovieId();
            } else {
                targetMovieId = danhSachPhimRef.push().getKey(); // Tạo ID mới
                if (targetMovieId == null) {
                    Toast.makeText(MovieManagementActivity.this, "Không thể tạo ID phim mới.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Dữ liệu cho danh_sach_phim (poster và ten_phim)
            Map<String, Object> basicMovieData = new HashMap<>();
            basicMovieData.put("ten_phim", title);
            basicMovieData.put("poster", imageUrl);

            // Dữ liệu cho chi_tiet_phim (tất cả các thông tin chi tiết)
            Map<String, Object> detailedMovieData = new HashMap<>();
            detailedMovieData.put("ten_phim", title); // Tiêu đề cũng có trong chi_tiet_phim
            detailedMovieData.put("mo_ta", description);
            detailedMovieData.put("trailer", trailerUrl);
            detailedMovieData.put("khoi_chieu", releaseDate);
            detailedMovieData.put("thoi_luong", duration);
            detailedMovieData.put("dao_dien", director);
            detailedMovieData.put("dien_vien", cast);
            detailedMovieData.put("danh_gia", rating);

            // Thực hiện cập nhật/thêm vào cả hai node
            Task<Void> updateBasicTask = danhSachPhimRef.child(targetMovieId).updateChildren(basicMovieData);
            Task<Void> updateDetailedTask = chiTietPhimRef.child(targetMovieId).updateChildren(detailedMovieData);

            Tasks.whenAllSuccess(updateBasicTask, updateDetailedTask)
                    .addOnSuccessListener(results -> {
                        Toast.makeText(MovieManagementActivity.this, isEditMode ? "Đã cập nhật phim thành công!" : "Đã thêm phim mới thành công!", Toast.LENGTH_SHORT).show();
                        // loadMovies(); // Tải lại danh sách sau khi cập nhật (có thể cần độ trễ nếu Firebase chưa sync kịp)
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MovieManagementActivity.this, "Lỗi khi lưu phim: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog(Movie movieToDelete) {
        new AlertDialog.Builder(this, R.style.AlertDialog_RedBlackTheme)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa phim '" + movieToDelete.getTitle() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (movieToDelete.getMovieId() != null) {
                        String movieId = movieToDelete.getMovieId();

                        // Xóa từ cả hai node
                        Task<Void> deleteTask1 = danhSachPhimRef.child(movieId).removeValue();
                        Task<Void> deleteTask2 = chiTietPhimRef.child(movieId).removeValue();

                        Tasks.whenAllSuccess(deleteTask1, deleteTask2)
                                .addOnSuccessListener(results -> {
                                    Toast.makeText(MovieManagementActivity.this, "Đã xóa phim: " + movieToDelete.getTitle(), Toast.LENGTH_SHORT).show();
                                    // loadMovies(); // Tải lại danh sách sau khi xóa (có thể cần độ trễ)
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MovieManagementActivity.this, "Lỗi khi xóa phim: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onEditClick(Movie movie) {
        showAddEditMovieDialog(movie);
    }

    @Override
    public void onDeleteClick(Movie movie) {
        showDeleteConfirmationDialog(movie);
    }
}