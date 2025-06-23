package com.example.movieee.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Import View
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // Import ImageView
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.movieee.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextPhone;
    private Button buttonSendOtp;
    private ImageView backArrow; // Thêm khai báo ImageView
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSendOtp = findViewById(R.id.buttonSendOtp);
        backArrow = findViewById(R.id.backArrow); // Ánh xạ ID cho nút back

        // Xử lý sự kiện click cho nút back
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Hoặc Intent để quay về LoginActivity
            }
        });

        buttonSendOtp.setOnClickListener(v -> {
            String phone = editTextPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                editTextPhone.setError("Phone number is required");
                return;
            }

            // Kiểm tra định dạng số điện thoại để loại bỏ số 0 ở đầu nếu có
            // và thêm mã quốc gia. Ví dụ: từ "0912345678" thành "+84912345678"
            String fullPhoneNumber;
            if (phone.startsWith("0")) {
                fullPhoneNumber = "+84" + phone.substring(1);
            } else {
                fullPhoneNumber = "+84" + phone; // Nếu đã nhập không có 0 ở đầu
            }

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(fullPhoneNumber) // Sử dụng số điện thoại đã chuẩn hóa
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(PhoneAuthCredential credential) {
                                    // Auto verify (rare)
                                }

                                @Override
                                public void onVerificationFailed(FirebaseException e) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCodeSent(@NonNull String verificationId,
                                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                    // Chuyển sang màn hình nhập OTP
                                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                                    intent.putExtra("verificationId", verificationId);
                                    intent.putExtra("phone", phone); // Truyền số điện thoại gốc (có thể có 0) để dùng cho các màn hình sau nếu cần hiển thị
                                    startActivity(intent);
                                }
                            })
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });
    }
}