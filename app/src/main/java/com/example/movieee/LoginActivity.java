package com.example.movieee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText loginPhone, loginPassWord;
    Button loginButton;
    TextView signupRedirectText, forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPhone = findViewById(R.id.loginphone);
        loginPassWord = findViewById(R.id.loginpw);
        loginButton = findViewById(R.id.buttonLogin);
        signupRedirectText = findViewById(R.id.textsignup);
        forgotPasswordText = findViewById(R.id.textForgotPassword); // ✅ Quên mật khẩu

        // Đăng nhập
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateUsername() | !validatePassWord()) {
                    // Không làm gì cả nếu dữ liệu không hợp lệ
                } else {
                    checkUser();
                }
            }
        });

        // Chuyển sang màn hình đăng ký
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        // ✅ Chuyển sang màn hình quên mật khẩu
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    public Boolean validateUsername() {
        String val = loginPhone.getText().toString();
        if (val.isEmpty()) {
            loginPhone.setError("Username cannot be empty");
            return false;
        } else {
            loginPhone.setError(null);
            return true;
        }
    }

    public Boolean validatePassWord() {
        String val = loginPassWord.getText().toString();
        if (val.isEmpty()) {
            loginPassWord.setError("Password cannot be empty");
            return false;
        } else {
            loginPassWord.setError(null);
            return true;
        }
    }

    public void checkUser() {
        String userUsername = loginPhone.getText().toString().trim();
        String userPassword = loginPassWord.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("phone").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    loginPhone.setError(null);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                        loginPassWord.setError(null);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        loginPassWord.setError("Invalid credentials");
                        loginPassWord.requestFocus();
                    }
                } else {
                    loginPhone.setError("User does not exist");
                    loginPhone.requestFocus();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
