package com.example.movieee;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth; // Thêm import này

public class SignupActivity extends AppCompatActivity {

    EditText signupUsername, signupPhone, signupEmail, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    CheckBox checkboxSignup;
    ImageView eyeIcon;

    FirebaseDatabase database;
    DatabaseReference reference;
    private FirebaseAuth mAuth; // Khai báo FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Ánh xạ các view với ID ĐÚNG từ activity_signup.xml
        signupUsername = findViewById(R.id.signup_username); // Đã sửa ID
        signupPhone = findViewById(R.id.signup_phone);     // Đã sửa ID
        signupEmail = findViewById(R.id.signup_email);     // Đã sửa ID
        signupPassword = findViewById(R.id.signup_pw);     // Đã sửa ID
        signupButton = findViewById(R.id.signup_button);   // Đã sửa ID
        loginRedirectText = findViewById(R.id.textsignin); // Đã sửa ID
        checkboxSignup = findViewById(R.id.checkBoxSignUp);      // Đã sửa ID (align với XML)
        eyeIcon = findViewById(R.id.eye_icon);             // Giữ nguyên, cần đảm bảo ID này có trong XML

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Đặt text checkbox có màu đỏ
        checkboxSignup.setText(Html.fromHtml("I agree with <font color='#FF0000'>privacy</font> and <font color='#FF0000'>policy</font>"));

        // Xử lý hiển thị/ẩn mật khẩu
        final boolean[] isPasswordVisible = {false};

        eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible[0]) {
                    // Ẩn mật khẩu
                    signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeIcon.setImageResource(R.drawable.ic_eye_off); // Cần drawable ic_eye_off
                } else {
                    // Hiện mật khẩu
                    signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eyeIcon.setImageResource(R.drawable.ic_eye); // Cần drawable ic_eye
                }
                isPasswordVisible[0] = !isPasswordVisible[0];
                signupPassword.setSelection(signupPassword.length()); // Giữ con trỏ ở cuối
            }
        });

        // Xử lý nút đăng ký
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = signupUsername.getText().toString().trim();
                String phone = signupPhone.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                // Kiểm tra đã đồng ý điều khoản chưa
                if (!checkboxSignup.isChecked()) {
                    Toast.makeText(SignupActivity.this, "Please agree to the privacy and policy terms", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra các trường nhập
                if (username.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // === BẮT ĐẦU THAY ĐỔI TRONG SIGNUPACTIVITY ===
                // 1. Tạo tài khoản trong Firebase Authentication trước
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Tài khoản Firebase Auth đã được tạo thành công
                                Toast.makeText(SignupActivity.this, "Đăng ký Auth thành công.", Toast.LENGTH_SHORT).show();

                                // 2. Sau đó, lưu dữ liệu chi tiết vào Realtime Database
                                database = FirebaseDatabase.getInstance();
                                reference = database.getReference("users");

                                String role = email.equals("admin@gmail.com") ? "admin" : "user";
                                HelperClass helperClass = new HelperClass(username, phone, email, password, role);

                                // Lưu dữ liệu vào Firebase sử dụng "user_" + email làm khóa
                                reference.child("user_" + email.replace(".", "_")).setValue(helperClass)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SignupActivity.this, "Bạn đã đăng ký thành công!!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Xảy ra lỗi khi lưu vào Realtime Database sau khi Auth thành công
                                            Toast.makeText(SignupActivity.this, "Lỗi khi lưu dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            // Có thể xóa tài khoản Auth vừa tạo nếu không muốn có dữ liệu không nhất quán
                                            if (mAuth.getCurrentUser() != null) {
                                                mAuth.getCurrentUser().delete();
                                            }
                                        });
                            } else {
                                // Đăng ký Firebase Auth thất bại (ví dụ: email đã tồn tại, mật khẩu quá yếu)
                                Toast.makeText(SignupActivity.this, "Đăng ký Auth thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                // === KẾT THÚC THAY ĐỔI TRONG SIGNUPACTIVITY ===
            }
        });

        // Chuyển sang màn hình đăng nhập
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}