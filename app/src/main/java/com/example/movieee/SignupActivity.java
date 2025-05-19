package com.example.movieee;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText signupFirstName,signupLastName,signupPhone,signupEmail,signupPassWord;
    TextView loginRedirectText;
    Button signupbutton;
    FirebaseDatabase database;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupFirstName=findViewById(R.id.textFN);
        signupLastName=findViewById(R.id.textLN);
        signupPhone=findViewById(R.id.textphone);
        signupEmail=findViewById(R.id.textemail);
        signupPassWord=findViewById(R.id.textpw);
        signupbutton=findViewById(R.id.buttonsignup);
        loginRedirectText = findViewById(R.id.textsignin);

        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String firstname = signupFirstName.getText().toString();
                String lastname = signupLastName.getText().toString();
                String phone = signupPhone.getText().toString();
                String email = signupEmail.getText().toString();
                String password = signupPassWord.getText().toString();

                HelperClass helperClass = new HelperClass(firstname,lastname,phone,email,password);
                reference.child(phone).setValue(helperClass);

                Toast.makeText(SignupActivity.this, "dang ky thanh cong", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }

        });
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        // Tìm CheckBox theo ID
        CheckBox checkBox = findViewById(R.id.checkboxsignup);
        // Đặt nội dung text với từ "privacy" màu đỏ (#FF0000)
        checkBox.setText(Html.fromHtml("I agree with <font color='#FF0000'>privacy</font> and <font color='#FF0000'>policy</font>"));


    }
}
