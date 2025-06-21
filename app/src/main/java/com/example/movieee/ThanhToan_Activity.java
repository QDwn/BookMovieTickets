package com.example.movieee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
// LOẠI BỎ: import java.util.concurrent.Executors;

// LOẠI BỎ: các imports cho ZaloPay và OkHttp
// import okhttp3.Call;
// import okhttp3.Callback;
// import okhttp3.MediaType;
// import okhttp3.OkHttpClient;
// import okhttp3.Request;
// import okhttp3.RequestBody;
// import okhttp3.Response;
// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONObject;
// import vn.zalopay.sdk.ZaloPaySDK;
// import vn.zalopay.sdk.listeners.PayOrderListener;

public class ThanhToan_Activity extends AppCompatActivity {

    private TextView tvMovieInfo, tvSelectedSeats, tvSeatPrice, tvComboPrice, tvTotalPrice;
    private Button btnConfirmPayment;

    private String movieId, ngay, diaDiem, gio;
    private String movieTitle; // Biến để lưu tên phim
    private ArrayList<String> selectedSeats;
    private int seatTotalPrice, comboTotalPrice;
    private int finalTotalAmount;

    private String userEmail;
    private String userName;

    // LOẠI BỎ: private OkHttpClient httpClient;
    // LOẠI BỎ: private static final String BACKEND_URL = "YOUR_BACKEND_SERVER_URL";
    // LOẠI BỎ: private static final int ZALOPAY_APP_ID = 2553;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        tvMovieInfo = findViewById(R.id.tvMovieInfo);
        tvSelectedSeats = findViewById(R.id.tvSelectedSeats);
        tvSeatPrice = findViewById(R.id.tvSeatPrice);
        tvComboPrice = findViewById(R.id.tvComboPrice);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        // LOẠI BỎ: httpClient = new OkHttpClient();

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            movieId = intent.getStringExtra("movieId");
            movieTitle = intent.getStringExtra("movieTitle"); // Nhận tên phim
            ngay = intent.getStringExtra("ngay");
            diaDiem = intent.getStringExtra("diaDiem");
            gio = intent.getStringExtra("gio");
            selectedSeats = intent.getStringArrayListExtra("selectedSeats");
            seatTotalPrice = intent.getIntExtra("seatTotalPrice", 0);
            comboTotalPrice = intent.getIntExtra("comboTotalPrice", 0);

            finalTotalAmount = seatTotalPrice + comboTotalPrice;

            // Đọc thông tin người dùng từ SharedPreferences
            SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            userEmail = sharedPref.getString("user_email", "guest");
            userName = sharedPref.getString("user_username", "Guest User");

            // Hiển thị thông tin
            tvMovieInfo.setText(String.format("Phim: %s\nNgày: %s\nRạp: %s\nGiờ: %s", movieTitle, ngay, diaDiem, gio));
            tvSelectedSeats.setText("Ghế đã chọn: " + (selectedSeats != null ? String.join(", ", selectedSeats) : "Không có"));

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

            tvSeatPrice.setText("Tổng tiền ghế: " + formatter.format(seatTotalPrice) + "đ");
            tvComboPrice.setText("Tổng tiền combo: " + formatter.format(comboTotalPrice) + "đ");
            tvTotalPrice.setText("Tổng cộng: " + formatter.format(finalTotalAmount) + "đ");
        }

        btnConfirmPayment.setOnClickListener(v -> {
            confirmPayment(); // Gọi lại phương thức thanh toán Firebase trực tiếp
        });

        // LOẠI BỎ: ZaloPaySDK.init(ZALOPAY_APP_ID);
    }

    // Phương thức confirmPayment (đã khôi phục và tinh chỉnh)
    private void confirmPayment() {
        if (selectedSeats == null || selectedSeats.isEmpty()) {
            Toast.makeText(this, "Không có ghế nào được chọn để thanh toán.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem ghế đã có người đặt chưa trước khi cập nhật
        DatabaseReference gheDaDatRef = FirebaseDatabase.getInstance()
                .getReference("ghe_da_dat")
                .child(movieId)
                .child(ngay)
                .child(gio);

        gheDaDatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> alreadyBookedSeats = new ArrayList<>();
                for (String seatId : selectedSeats) {
                    if (snapshot.hasChild(seatId) && Boolean.TRUE.equals(snapshot.child(seatId).getValue(Boolean.class))) {
                        alreadyBookedSeats.add(seatId);
                    }
                }

                if (!alreadyBookedSeats.isEmpty()) {
                    // Có ghế đã được đặt bởi người khác
                    Toast.makeText(ThanhToan_Activity.this, "Ghế " + String.join(", ", alreadyBookedSeats) + " đã có người đặt. Vui lòng chọn ghế khác.", Toast.LENGTH_LONG).show();
                    // Điều hướng về lại màn hình chọn ghế
                    Intent backToSeatSelection = new Intent(ThanhToan_Activity.this, DatGhe_Activity.class);
                    backToSeatSelection.putExtra("movieId", movieId);
                    backToSeatSelection.putExtra("movieTitle", movieTitle); // Quan trọng: Truyền lại tên phim
                    backToSeatSelection.putExtra("ngay", ngay);
                    backToSeatSelection.putExtra("diaDiem", diaDiem);
                    backToSeatSelection.putExtra("gio", gio);
                    startActivity(backToSeatSelection);
                    finish();
                } else {
                    // Tất cả các ghế đều có sẵn, tiến hành cập nhật
                    Map<String, Object> seatUpdates = new HashMap<>();
                    for (String seatId : selectedSeats) {
                        seatUpdates.put(seatId, true); // Đặt trạng thái ghế là đã đặt
                    }

                    gheDaDatRef.updateChildren(seatUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Lưu thông tin đặt vé chi tiết vào một node khác, ví dụ "lich_su_dat_ve"
                                        DatabaseReference lichSuDatVeRef = FirebaseDatabase.getInstance().getReference("lich_su_dat_ve");

                                        // Tạo một ID duy nhất cho mỗi giao dịch đặt vé
                                        String bookingId = lichSuDatVeRef.push().getKey();

                                        if (bookingId != null) {
                                            Map<String, Object> bookingDetails = new HashMap<>();
                                            bookingDetails.put("movieId", movieId);
                                            bookingDetails.put("movieTitle", movieTitle); // Lưu tên phim
                                            bookingDetails.put("ngayChieu", ngay);
                                            bookingDetails.put("diaDiem", diaDiem);
                                            bookingDetails.put("gioChieu", gio);
                                            bookingDetails.put("gheDaChon", selectedSeats);
                                            bookingDetails.put("tongTienGhe", seatTotalPrice);
                                            bookingDetails.put("tongTienCombo", comboTotalPrice);
                                            bookingDetails.put("tongCong", finalTotalAmount);
                                            bookingDetails.put("userEmail", userEmail); // Lưu email người dùng
                                            bookingDetails.put("userName", userName); // Lưu tên người dùng
                                            // Bạn có thể thêm timestamp ở đây
                                            bookingDetails.put("timestamp", System.currentTimeMillis());

                                            lichSuDatVeRef.child(bookingId).setValue(bookingDetails)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> detailTask) {
                                                            if (detailTask.isSuccessful()) {
                                                                Toast.makeText(ThanhToan_Activity.this, "Thanh toán thành công! Ghế đã được đặt.", Toast.LENGTH_LONG).show();
                                                                // Chuyển về màn hình chính hoặc màn hình xác nhận đặt vé thành công
                                                                Intent intent = new Intent(ThanhToan_Activity.this, MainActivity2.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);
                                                                finish(); // Đóng activity hiện tại
                                                            } else {
                                                                Toast.makeText(ThanhToan_Activity.this, "Lỗi khi lưu chi tiết đặt vé: " + detailTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(ThanhToan_Activity.this, "Không thể tạo ID đặt vé.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(ThanhToan_Activity.this, "Lỗi khi cập nhật ghế: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThanhToan_Activity.this, "Lỗi kiểm tra ghế: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}