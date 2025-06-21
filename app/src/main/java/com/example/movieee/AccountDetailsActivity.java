package com.example.movieee;

import android.app.AlertDialog; // Có thể xóa nếu không dùng AlertDialog nào khác
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater; // Có thể xóa nếu không dùng LayoutInflater nào khác
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // Có thể xóa nếu không dùng EditText nào khác
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap; // Có thể xóa nếu không dùng HashMap nào khác
import java.util.Map; // Có thể xóa nếu không dùng Map nào khác

public class AccountDetailsActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvPhone, tvRole;
    private Button btnLogout; // Đã xóa btnEditProfile
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        tvUsername = findViewById(R.id.tv_detail_username);
        tvEmail = findViewById(R.id.tv_detail_email);
        tvPhone = findViewById(R.id.tv_detail_phone);
        tvRole = findViewById(R.id.tv_detail_role);
        btnLogout = findViewById(R.id.btn_logout);
        // btnEditProfile đã được xóa

        mAuth = FirebaseAuth.getInstance();

        // Lấy email người dùng từ Intent
        currentUserEmail = getIntent().getStringExtra("userEmail");

        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            loadUserDetails(currentUserEmail);
        } else {
            // Nếu không có email được truyền, thử lấy từ Firebase Auth
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null && firebaseUser.getEmail() != null) {
                currentUserEmail = firebaseUser.getEmail();
                loadUserDetails(currentUserEmail);
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
                // Chuyển về màn hình đăng nhập nếu không có thông tin user
                Intent intent = new Intent(AccountDetailsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Đăng xuất khỏi Firebase Authentication
            Intent intent = new Intent(AccountDetailsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(AccountDetailsActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
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
                        tvUsername.setText("Tên người dùng: " + user.getUsername());
                        tvEmail.setText("Email: " + user.getEmail());
                        tvPhone.setText("Số điện thoại: " + user.getPhone());
                        tvRole.setText("Vai trò: " + user.getRole());
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

    // Các phương thức showEditProfileDialog() và updateUserProfileInDatabase() đã được xóa
}