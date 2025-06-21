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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MovieManagementActivity extends AppCompatActivity implements MovieManagementAdapter.OnItemClickListener {

    private RecyclerView moviesRecyclerView;
    private MovieManagementAdapter movieAdapter;
    private List<Movie> movieList;
    private DatabaseReference danhSachPhimRef; // Ref cho danh_sach_phim (poster, title)
    private DatabaseReference chiTietPhimRef;  // Ref cho chi_tiet_phim (full details)
    private DatabaseReference counterRef; // Ref cho bộ đếm ID
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
        counterRef = FirebaseDatabase.getInstance().getReference("counters").child("movieIdCounter"); // Khởi tạo counterRef

        movieList = new ArrayList<>();
        movieAdapter = new MovieManagementAdapter(movieList, this);
        moviesRecyclerView.setAdapter(movieAdapter);

        movieAdapter.setOnItemClickListener(this);

        loadMovies();

        btnAddMovie.setOnClickListener(v -> showAddEditMovieDialog(null));
    }

    private void loadMovies() {
        movieList.clear();
        // Lấy thông tin cơ bản từ danh_sach_phim
        danhSachPhimRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot danhSachSnapshot) {
                // Sử dụng AtomicInteger để theo dõi số lượng chi tiết phim đã tải xong
                AtomicInteger pendingDetailsFetches = new AtomicInteger(0);

                if (!danhSachSnapshot.exists()) {
                    movieAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot movieBriefSnapshot : danhSachSnapshot.getChildren()) {
                    String movieId = movieBriefSnapshot.getKey();
                    String title = movieBriefSnapshot.child("ten_phim").getValue(String.class);
                    String imageUrl = movieBriefSnapshot.child("poster").getValue(String.class);

                    if (movieId != null && title != null && imageUrl != null) {
                        pendingDetailsFetches.incrementAndGet(); // Tăng số lượng chờ tải chi tiết

                        // Sau đó, lấy thông tin chi tiết từ chi_tiet_phim
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
                                List<String> theLoai = detailSnapshot.child("the_loai").getValue(t);

                                // Tạo đối tượng Movie hoàn chỉnh
                                Movie movie = new Movie(title, imageUrl, movieId, description, trailerUrl,
                                        releaseDate, duration, director, castList,theLoai, rating);
                                movieList.add(movie);

                                // Giảm số lượng chờ và kiểm tra nếu tất cả đã tải xong
                                if (pendingDetailsFetches.decrementAndGet() == 0) {
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
            Double currentRating = 0.0;
            try {
                currentRating = Double.parseDouble(etRating.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Đánh giá phải là số.", Toast.LENGTH_SHORT).show();
                return;
            }

            final Double finalRating = currentRating; // Biến effectively final

            if (title.isEmpty() || imageUrl.isEmpty() || description.isEmpty() ||
                    releaseDate.isEmpty() || duration.isEmpty() || director.isEmpty() || cast.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đủ thông tin bắt buộc.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditMode) {
                String targetMovieId = movieToEdit.getMovieId();

                // Dữ liệu cho danh_sach_phim (poster và ten_phim)
                Map<String, Object> basicMovieData = new HashMap<>();
                basicMovieData.put("ten_phim", title);
                basicMovieData.put("poster", imageUrl);
                basicMovieData.put("movieId", targetMovieId); // Luôn lưu movieId

                // Dữ liệu cho chi_tiet_phim (tất cả các thông tin chi tiết)
                Map<String, Object> detailedMovieData = new HashMap<>();
                detailedMovieData.put("ten_phim", title); // Tiêu đề cũng có trong chi_tiet_phim
                detailedMovieData.put("mo_ta", description);
                detailedMovieData.put("trailer", trailerUrl);
                detailedMovieData.put("khoi_chieu", releaseDate);
                detailedMovieData.put("thoi_luong", duration);
                detailedMovieData.put("dao_dien", director);
                detailedMovieData.put("dien_vien", cast);
                detailedMovieData.put("danh_gia", finalRating); // Sử dụng finalRating

                // Thực hiện cập nhật vào cả hai node
                Task<Void> updateBasicTask = danhSachPhimRef.child(targetMovieId).updateChildren(basicMovieData);
                Task<Void> updateDetailedTask = chiTietPhimRef.child(targetMovieId).updateChildren(detailedMovieData);

                Tasks.whenAllSuccess(updateBasicTask, updateDetailedTask)
                        .addOnSuccessListener(results -> {
                            Toast.makeText(MovieManagementActivity.this, "Đã cập nhật phim thành công!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MovieManagementActivity.this, "Lỗi khi lưu phim: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

            } else {
                // Logic để tăng ID tự động khi thêm phim mới
                counterRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Integer currentId = currentData.getValue(Integer.class);
                        if (currentId == null) {
                            currentData.setValue(1); // Khởi tạo nếu chưa có
                        } else {
                            currentData.setValue(currentId + 1);
                        }
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                        if (committed) {
                            Long newIdLong = currentData.getValue(Long.class);
                            if (newIdLong != null) {
                                // Dòng này đã được sửa để thêm tiền tố "id_phim"
                                String newMovieId = "id_phim" + String.valueOf(newIdLong);

                                // Dữ liệu cho danh_sach_phim
                                Map<String, Object> basicMovieData = new HashMap<>();
                                basicMovieData.put("ten_phim", title);
                                basicMovieData.put("poster", imageUrl);
                                basicMovieData.put("movieId", newMovieId); // Lưu movieId vào đây

                                // Dữ liệu cho chi_tiet_phim
                                Map<String, Object> detailedMovieData = new HashMap<>();
                                detailedMovieData.put("ten_phim", title);
                                detailedMovieData.put("mo_ta", description);
                                detailedMovieData.put("trailer", trailerUrl);
                                detailedMovieData.put("khoi_chieu", releaseDate);
                                detailedMovieData.put("thoi_luong", duration);
                                detailedMovieData.put("dao_dien", director);
                                detailedMovieData.put("dien_vien", cast);
                                detailedMovieData.put("danh_gia", finalRating); // Sử dụng finalRating

                                // Thực hiện thêm vào cả hai node với ID mới
                                Task<Void> addTaskBasic = danhSachPhimRef.child(newMovieId).setValue(basicMovieData);
                                Task<Void> addTaskDetailed = chiTietPhimRef.child(newMovieId).setValue(detailedMovieData);

                                Tasks.whenAllSuccess(addTaskBasic, addTaskDetailed)
                                        .addOnSuccessListener(results -> {
                                            Toast.makeText(MovieManagementActivity.this, "Đã thêm phim mới với ID: " + newMovieId, Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MovieManagementActivity.this, "Lỗi khi thêm phim: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                Toast.makeText(MovieManagementActivity.this, "Lỗi: Không lấy được ID mới.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MovieManagementActivity.this, "Lỗi giao dịch ID: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
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