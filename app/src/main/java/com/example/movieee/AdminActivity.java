package com.example.movieee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        TextView textView = findViewById(R.id.admin_text);
        textView.setText("Welcome, Admin!");
        Button btnManageUsers = findViewById(R.id.btnManageUsers);
        Button btnManageMovies = findViewById(R.id.btnManageMovies);

        btnManageUsers.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, UserManagementActivity.class);
                startActivity(intent);
            }
        });

        // Thêm Listener cho nút Quản lý Phim
        btnManageMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, MovieManagementActivity.class);
                startActivity(intent);
            }
        });
    }
}