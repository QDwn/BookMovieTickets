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

    private ShapeableImageView ivProfileAvatar;
    private TextView tvUsernameDisplay, tvEmailDisplay, tvPhoneDisplay;
    private Button btnLogout;
    private ImageView backArrow;
    private LinearLayout layoutMyTicket, layoutChangePassword;

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);


        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        tvUsernameDisplay = findViewById(R.id.tv_username_display);
        tvEmailDisplay = findViewById(R.id.tv_email_display);
        tvPhoneDisplay = findViewById(R.id.tv_phone_display);
        btnLogout = findViewById(R.id.btn_logout);
        backArrow = findViewById(R.id.back_arrow);
        layoutMyTicket = findViewById(R.id.layout_my_ticket);
        layoutChangePassword = findViewById(R.id.layout_change_password);

        mAuth = FirebaseAuth.getInstance();


        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPref.getString("user_email", null);

        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null && firebaseUser.getEmail() != null) {
                currentUserEmail = firebaseUser.getEmail();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("user_email", currentUserEmail);
                editor.apply();
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AccountDetailsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }

        loadUserDetails(currentUserEmail);


        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
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

        backArrow.setOnClickListener(v -> onBackPressed());

        layoutMyTicket.setOnClickListener(v -> {
            Intent intent = new Intent(AccountDetailsActivity.this, UserTicketsActivity.class);
            startActivity(intent);
        });

        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(AccountDetailsActivity.this, ForgotPasswordActivity.class); // Giả sử dùng ForgotPasswordActivity để reset mật khẩu
            intent.putExtra("userEmailForReset", currentUserEmail);
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