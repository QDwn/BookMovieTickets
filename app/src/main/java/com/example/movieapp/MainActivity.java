package com.example.movieapp;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout notificationPanel;
    private boolean isPanelShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Ánh xạ panel thông báo
        notificationPanel = findViewById(R.id.notification_panel);

        // Xử lý sự kiện nút chuông
        ImageButton bellButton = findViewById(R.id.bell_icon);
        bellButton.setOnClickListener(v -> toggleNotificationPanel());
    }

    private void toggleNotificationPanel() {
        if (isPanelShown) {
            // Ẩn panel (trượt sang phải)
            notificationPanel.animate()
                    .translationX(notificationPanel.getWidth())
                    .setDuration(300)
                    .withEndAction(() -> notificationPanel.setVisibility(View.GONE));
        } else {
            // Hiện panel (trượt từ phải sang)
            notificationPanel.setVisibility(View.VISIBLE);
            notificationPanel.setTranslationX(notificationPanel.getWidth());
            notificationPanel.animate()
                    .translationX(0)
                    .setDuration(300);
        }
        isPanelShown = !isPanelShown;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isPanelShown) {
            Rect viewRect = new Rect();
            notificationPanel.getGlobalVisibleRect(viewRect);
            if (!viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                toggleNotificationPanel();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    public void onMenuButtonClick(View view) {
        int id = view.getId();

        if (id == R.id.btn_home) {
            // Xử lý khi click Home
        } else if (id == R.id.btn_ticket) {
            // Xử lý khi click Ticket
        } else if (id == R.id.btn_movie) {
            // Xử lý khi click Movie
        } else if (id == R.id.btn_account) {
            // Xử lý khi click Account
        }
    }
    }