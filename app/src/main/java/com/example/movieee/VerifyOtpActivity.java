package com.example.movieee;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText editTextOtp, editTextNewPassword;
    private Button buttonVerify;
    private String verificationId, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        editTextOtp = findViewById(R.id.editTextOtp);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonVerify = findViewById(R.id.buttonVerify);

        verificationId = getIntent().getStringExtra("verificationId");
        phone = getIntent().getStringExtra("phone");

        buttonVerify.setOnClickListener(v -> {
            String code = editTextOtp.getText().toString().trim();
            String newPassword = editTextNewPassword.getText().toString().trim();

            if (code.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // ✅ Sau khi xác thực thành công, cập nhật mật khẩu trong Realtime Database
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

                            // Tìm user có số điện thoại bằng với "phone"
                            reference.orderByChild("phone").equalTo(phone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                                    userSnapshot.getRef().child("password").setValue(newPassword);
                                                }
                                                Toast.makeText(VerifyOtpActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                finish(); // Quay lại màn hình trước hoặc thoát
                                            } else {
                                                Toast.makeText(VerifyOtpActivity.this, "Phone number not found in database", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            Toast.makeText(VerifyOtpActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(this, "Verification Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
