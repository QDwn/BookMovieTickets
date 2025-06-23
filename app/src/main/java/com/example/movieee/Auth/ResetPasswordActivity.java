package com.example.movieee.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movieee.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextNewPassword, editTextConfirmPassword;
    private Button buttonResetPassword;
    private String userPhone; // Để lưu số điện thoại của người dùng cần đặt lại mật khẩu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);

        // Lấy số điện thoại từ Intent (được truyền từ VerifyOtpActivity)
        if (getIntent() != null) {
            userPhone = getIntent().getStringExtra("phone");
        }

        buttonResetPassword.setOnClickListener(v -> {
            String newPassword = editTextNewPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(ResetPasswordActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(ResetPasswordActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userPhone != null && !userPhone.isEmpty()) {
                updatePasswordInDatabase(userPhone, newPassword);
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Phone number is missing. Cannot reset password.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePasswordInDatabase(String phone, String newPassword) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        // Firebase Realtime Database sẽ tìm đến child "users/số_điện_thoại"
        // và cập nhật trường "password"
        Map<String, Object> passwordUpdate = new HashMap<>();
        passwordUpdate.put("password", newPassword);

        reference.child(phone).updateChildren(passwordUpdate)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                        // Chuyển về màn hình đăng nhập sau khi đổi mật khẩu
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Failed to reset password: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}