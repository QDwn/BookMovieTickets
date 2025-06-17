// app/src/main/java/com/example/bmtadr/LoginActivity.java
package com.example.bmtadr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast; // Import Toast

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_pw);
        loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Thay đổi từ | thành ||
                if (!validatePhone() || !validatePassword()){
                    // Hiển thị thông báo nếu xác thực thất bại
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    checkUser();
                }
            }
        });
    }

    public Boolean validatePhone(){
        String val = loginEmail.getText().toString();
        if (val.isEmpty()){
            loginEmail.setError("Phone cannot be empty");
            return false;
        } else {
            loginEmail.setError(null);
            return true;
        }
    }

    public Boolean validatePassword(){
        String val = loginPassword.getText().toString();
        if (val.isEmpty()){
            loginPassword.setError("Password cannot be empty");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser(){
        String userEmail = loginEmail.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        // Quan trọng: đảm bảo emailKey này khớp với cách bạn lưu người dùng khi đăng ký.
        // Nếu loginEmail thực sự là số điện thoại, bạn cần thay đổi logic ở đây.
        // Hiện tại, code này vẫn đang mong đợi email.
        String emailKey = "user_" + userEmail.replace(".", "_");

        reference.child(emailKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String passwordFromDB = snapshot.child("password").getValue(String.class);

                    if (userPassword.equals(passwordFromDB)) {
                        String roleFromDB = snapshot.child("role").getValue(String.class);

                        if ("admin".equals(roleFromDB)) {
                            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, UserActivity.class));
                        }
                        finish();
                    } else {
                        loginPassword.setError("Sai mật khẩu");
                        loginPassword.requestFocus();
                    }
                } else {
                    // Log ra lỗi hoặc hiển thị Toast chi tiết hơn
                    loginEmail.setError("Email không tồn tại");
                    loginEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi từ Firebase (ví dụ: mất kết nối, quyền truy cập)
                Toast.makeText(LoginActivity.this, "Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}