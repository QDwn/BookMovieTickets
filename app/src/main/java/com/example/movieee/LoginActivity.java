package com.example.movieee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    TextView signupRedirectText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        loginPhone = findViewById(R.id.loginphone);
        loginPassWord = findViewById(R.id.loginpw);
        signupRedirectText = findViewById(R.id.textsignup);
        loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateUsername() | !validatePassWord()){

                }else{
                    checkUser();
                }
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
            }
        });

        // Gán sự kiện nhấn vào "Sign Up"
        TextView textViewSignUp = findViewById(R.id.textsignup);
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình đăng ký
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }

        });
    }
    public Boolean validateUsername(){
        String val = loginPhone.getText().toString();
        if (val.isEmpty()){
            loginPhone.setError("Username cannot be empty");
            return false;
        }else{
            loginPhone.setError(null);
            return true;
        }
    }

    public Boolean validatePassWord(){
        String val = loginPassWord.getText().toString();
        if (val.isEmpty()){
            loginPassWord.setError("Password cannot be empty");
            return false;
        }else{
            loginPassWord.setError(null);
            return true;
        }
    }

    public void checkUser(){
        String userUsername = loginPhone.getText().toString().trim();
        String userPassword = loginPassWord.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

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

            }
        });
    }
}
