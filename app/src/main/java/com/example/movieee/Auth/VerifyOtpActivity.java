package com.example.movieee.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.movieee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException; // Đảm bảo import này được thêm vào
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions; // Đảm bảo import này được thêm vào
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit; // Đảm bảo import này được thêm vào

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText editTextOtp;
    private Button buttonVerifyOtp;
    private TextView textResendOtp;
    private FirebaseAuth mAuth;
    private String verificationId;
    private String phoneNumber; // Để lưu số điện thoại và sử dụng lại nếu cần gửi lại OTP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        mAuth = FirebaseAuth.getInstance();
        editTextOtp = findViewById(R.id.editTextOtp);
        buttonVerifyOtp = findViewById(R.id.buttonVerifyOtp);
        textResendOtp = findViewById(R.id.textResendOtp);

        // Lấy verificationId và số điện thoại từ Intent
        if (getIntent() != null) {
            verificationId = getIntent().getStringExtra("verificationId");
            phoneNumber = getIntent().getStringExtra("phone");
        }

        buttonVerifyOtp.setOnClickListener(v -> {
            String otp = editTextOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                editTextOtp.setError("Please enter OTP");
                return;
            }
            if (verificationId != null) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                signInWithPhoneAuthCredential(credential);
            } else {
                Toast.makeText(VerifyOtpActivity.this, "Verification ID is missing.", Toast.LENGTH_SHORT).show();
            }
        });

        textResendOtp.setOnClickListener(v -> {
            // Gửi lại OTP
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                // Firebase yêu cầu số điện thoại phải có mã quốc gia, ví dụ "+84xxxxxxxxxx"
                // Bạn cần đảm bảo 'phoneNumber' đã được chuẩn hóa đúng định dạng này
                // Nếu 'phoneNumber' từ ForgotPasswordActivity là "0912345678", bạn cần chuyển đổi nó
                // như đã làm trong ForgotPasswordActivity: "+84" + phone.substring(1)
                String fullPhoneNumber = "+84" + phoneNumber.substring(1);
                resendVerificationCode(fullPhoneNumber); // Gửi lại OTP
            } else {
                Toast.makeText(VerifyOtpActivity.this, "Phone number is missing to resend OTP.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // OTP hợp lệ, chuyển đến màn hình đổi mật khẩu (hoặc MainActivity)
                            Toast.makeText(VerifyOtpActivity.this, "OTP Verified successfully!", Toast.LENGTH_SHORT).show();
                            // TODO: Chuyển đến màn hình đổi mật khẩu hoặc reset mật khẩu ở đây
                            // Chuyển đến màn hình đặt lại mật khẩu sau khi OTP được xác minh
                            Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                            intent.putExtra("phone", phoneNumber); // Truyền số điện thoại để ResetPasswordActivity biết người dùng nào
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // OTP không hợp lệ
                            // Lỗi ở đây có thể là do OTP sai hoặc một số lý do Firebase khác
                            Toast.makeText(VerifyOtpActivity.this, "OTP Verification Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void resendVerificationCode(String phoneNumberWithCountryCode) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Tự động xác minh (hiếm khi xảy ra)
                        // Trong trường hợp này, Firebase có thể đã tự động xác minh số điện thoại
                        // Bạn có thể xử lý việc đăng nhập hoặc chuyển hướng ở đây
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) { // CHỮ KÝ PHƯƠNG THỨC ĐƯỢC SỬA ĐÚNG
                        // Xác minh không thành công, có thể do định dạng số điện thoại sai,
                        // hoặc do lỗi khác từ Firebase (ví dụ: bị chặn, giới hạn số lần gửi)
                        Toast.makeText(VerifyOtpActivity.this, "Resend Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // Mã OTP đã được gửi lại thành công
                        verificationId = newVerificationId; // Cập nhật verificationId mới
                        Toast.makeText(VerifyOtpActivity.this, "OTP Resent!", Toast.LENGTH_SHORT).show();
                    }
                };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumberWithCountryCode) // Sử dụng số điện thoại đã có mã quốc gia
                        .setTimeout(60L, TimeUnit.SECONDS) // Thời gian chờ OTP
                        .setActivity(this) // Context của Activity hiện tại
                        .setCallbacks(mCallbacks) // Callback để xử lý các sự kiện xác minh
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}