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

public class SignupActivity extends AppCompatActivity {

    EditText signupUsername, signupPhone, signupEmail, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    CheckBox checkboxSignup;
    ImageView eyeIcon;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Ánh xạ các view
        signupUsername = findViewById(R.id.textUsername);
        signupPhone = findViewById(R.id.textphone);
        signupEmail = findViewById(R.id.textemail);
        signupPassword = findViewById(R.id.textpw);
        signupButton = findViewById(R.id.buttonsignup);
        loginRedirectText = findViewById(R.id.textsignin);
        checkboxSignup = findViewById(R.id.checkboxsignup);
        eyeIcon = findViewById(R.id.eye_icon);

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
                    eyeIcon.setImageResource(R.drawable.ic_eye_off);
                } else {
                    // Hiện mật khẩu
                    signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eyeIcon.setImageResource(R.drawable.ic_eye);
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

                // Ghi dữ liệu vào Firebase
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                HelperClass helperClass = new HelperClass(username, phone, email, password);
                reference.child(phone).setValue(helperClass);

                Toast.makeText(SignupActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
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
