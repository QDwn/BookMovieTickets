package com.example.movieee.Auth;

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

import com.example.movieee.Model.HelperClass;
import com.example.movieee.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    EditText signupUsername, signupPhone, signupEmail, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    CheckBox checkboxSignup;
    ImageView eyeIcon;

    FirebaseDatabase database;
    DatabaseReference reference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        signupUsername = findViewById(R.id.signup_username);
        signupPhone = findViewById(R.id.signup_phone);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_pw);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.textsignin);
        checkboxSignup = findViewById(R.id.checkBoxSignUp);
        eyeIcon = findViewById(R.id.eye_icon);


        mAuth = FirebaseAuth.getInstance();


        checkboxSignup.setText(Html.fromHtml("I agree with <font color='#FF0000'>privacy</font> and <font color='#FF0000'>policy</font>"));

        // Xử lý hiển thị/ẩn mật khẩu
        final boolean[] isPasswordVisible = {false};

        eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible[0]) {
                    // Ẩn
                    signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeIcon.setImageResource(R.drawable.ic_eye_off);
                } else {
                    // Hiện
                    signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eyeIcon.setImageResource(R.drawable.ic_eye);
                }
                isPasswordVisible[0] = !isPasswordVisible[0];
                signupPassword.setSelection(signupPassword.length());
            }
        });

        //  đăng ký
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = signupUsername.getText().toString().trim();
                String phone = signupPhone.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                if (!checkboxSignup.isChecked()) {
                    Toast.makeText(SignupActivity.this, "Please agree to the privacy and policy terms", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (username.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }



                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Tài khoản Firebase Auth đã được tạo thành công
                                Toast.makeText(SignupActivity.this, "Đăng ký thành công.", Toast.LENGTH_SHORT).show();

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
                                            Toast.makeText(SignupActivity.this, "Lỗi khi lưu dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            if (mAuth.getCurrentUser() != null) {
                                                mAuth.getCurrentUser().delete();
                                            }
                                        });
                            } else {
                                // Đăng ký Firebase Auth thất bại (ví dụ: email đã tồn tại, mật khẩu quá yếu)
                                Toast.makeText(SignupActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}