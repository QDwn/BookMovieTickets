package com.example.movieee.Home;

import android.content.Context; // Thêm import này
import android.content.Intent;
import android.content.SharedPreferences; // Thêm import này
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView; // Thêm ImageView
import android.widget.LinearLayout; // Thêm LinearLayout
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.movieee.Auth.ForgotPasswordActivity;
import com.example.movieee.Auth.LoginActivity;
import com.example.movieee.Model.HelperClass;
import com.example.movieee.R;
import com.google.android.material.imageview.ShapeableImageView; // Thêm ShapeableImageView
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountDetailsActivity extends AppCompatActivity {

    private ShapeableImageView ivProfileAvatar; // Avatar
    private TextView tvUsernameDisplay, tvEmailDisplay, tvPhoneDisplay; // Các TextView mới
    private Button btnLogout;
    private ImageView backArrow; // Nút quay lại
    private LinearLayout layoutMyTicket, layoutChangePassword; // Các layout có thể click

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        // Ánh xạ các View mới
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        tvUsernameDisplay = findViewById(R.id.tv_username_display);
        tvEmailDisplay = findViewById(R.id.tv_email_display);
        tvPhoneDisplay = findViewById(R.id.tv_phone_display);
        btnLogout = findViewById(R.id.btn_logout);
        backArrow = findViewById(R.id.back_arrow);
        layoutMyTicket = findViewById(R.id.layout_my_ticket);
        layoutChangePassword = findViewById(R.id.layout_change_password);

        mAuth = FirebaseAuth.getInstance();

        // Lấy email người dùng từ SharedPreferences hoặc Intent
        // Ưu tiên SharedPreferences vì nó lưu trữ trạng thái đăng nhập
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPref.getString("user_email", null);

        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            // Nếu không có email trong SharedPreferences, thử lấy từ Firebase Auth
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null && firebaseUser.getEmail() != null) {
                currentUserEmail = firebaseUser.getEmail();
                // Lưu vào SharedPreferences cho lần sau
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("user_email", currentUserEmail);
                editor.apply();
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AccountDetailsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return; // Kết thúc hàm nếu không có người dùng
            }
        }

        loadUserDetails(currentUserEmail);

        // Đặt listener cho nút "Đăng xuất"
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // Xóa thông tin đăng nhập đã lưu
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("user_email");
            editor.remove("user_username");
            editor.apply();

            Intent intent = new Intent(AccountDetailsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(AccountDetailsActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        });

        // Đặt listener cho nút quay lại
        backArrow.setOnClickListener(v -> onBackPressed());

        // Đặt listener cho mục "Vé của tôi"
        layoutMyTicket.setOnClickListener(v -> {
            Intent intent = new Intent(AccountDetailsActivity.this, UserTicketsActivity.class);
            startActivity(intent);
        });

        // Đặt listener cho mục "Đổi mật khẩu"
        layoutChangePassword.setOnClickListener(v -> {
            // Điều hướng đến ResetPasswordActivity (hoặc một Activity đổi mật khẩu khác)
            // Cần truyền thông tin cần thiết nếu ResetPasswordActivity yêu cầu (ví dụ: email/phone)
            Intent intent = new Intent(AccountDetailsActivity.this, ForgotPasswordActivity.class); // Giả sử dùng ForgotPasswordActivity để reset mật khẩu
            intent.putExtra("userEmailForReset", currentUserEmail); // Truyền email
            startActivity(intent);
        });
    }

    private void loadUserDetails(String email) {
        String emailKey = email.replace(".", "_");
        userRef = FirebaseDatabase.getInstance().getReference("users").child("user_" + emailKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HelperClass user = snapshot.getValue(HelperClass.class);
                    if (user != null) {
                        tvUsernameDisplay.setText(user.getUsername());
                        tvEmailDisplay.setText(user.getEmail());
                        tvPhoneDisplay.setText(user.getPhone());
                        // Vai trò không hiển thị trên giao diện mới, nhưng bạn có thể giữ nó ở đây nếu cần cho logic khác.
                        // tvRole.setText("Vai trò: " + user.getRole());

                        // Tải ảnh đại diện nếu có
                        // Hiện tại đang dùng drawable/user, nếu có URL ảnh trong Firebase có thể tải bằng Glide
                        // Ví dụ: Glide.with(AccountDetailsActivity.this).load(user.getProfileImageUrl()).into(ivProfileAvatar);
                        if ("admin".equals(user.getRole())) {
                            ivProfileAvatar.setImageResource(R.drawable.admin);
                        } else {
                            ivProfileAvatar.setImageResource(R.drawable.user);
                        }

                    } else {
                        Toast.makeText(AccountDetailsActivity.this, "Không thể tải dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                        if (mAuth.getCurrentUser() != null) {
                            mAuth.signOut();
                        }
                        Intent intent = new Intent(AccountDetailsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(AccountDetailsActivity.this, "Người dùng không tồn tại trong cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.signOut();
                    }
                    Intent intent = new Intent(AccountDetailsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountDetailsActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}