package com.example.bmtadr;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserManagementActivity extends AppCompatActivity {

    ListView userListView;
    DatabaseReference usersRef;
    ArrayList<String> userList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management); // Bạn cần tạo tệp layout này

        userListView = findViewById(R.id.userListView);
        usersRef = FirebaseDatabase.getInstance().getReference("users"); // Tham chiếu đến node 'users'

        userList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        userListView.setAdapter(adapter);
        userListView.setOnItemClickListener((parent, view, position, id) -> {
            String userInfo = userList.get(position);
        });
        loadUsers();
    }
    private String extractEmailFromUserInfo(String userInfo) {
        // Ví dụ: Chuỗi là "Email: abc@example.com, Phone: ..., Role: ..."
        try {
            int emailStart = userInfo.indexOf("Email: ") + "Email: ".length();
            int emailEnd = userInfo.indexOf(",", emailStart);
            if (emailEnd == -1) { // Trường hợp email là phần tử cuối cùng
                emailEnd = userInfo.length();
            }
            String email = userInfo.substring(emailStart, emailEnd).trim();
            return email;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showUserOptionsDialog(String email) {
        // Tạo emailKey tương tự như cách bạn lưu trong SignupActivity và LoginActivity
        String emailKey = "user_" + email.replace(".", "_"); //

        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn người dùng")
                .setMessage("Bạn muốn làm gì với người dùng " + email + "?")
                .setPositiveButton("Sửa", (dialog, which) -> {

                    Toast.makeText(UserManagementActivity.this, "Chức năng sửa cho " + email, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Xóa", (dialog, which) -> {
                    usersRef.child(emailKey).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(UserManagementActivity.this, "Đã xóa người dùng: " + email, Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(UserManagementActivity.this, "Lỗi khi xóa người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNeutralButton("Hủy", null)
                .show();
    }
    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear(); // Xóa danh sách cũ để cập nhật
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    HelperClass user = userSnapshot.getValue(HelperClass.class);
                    if (user != null) {
                        // Thêm thông tin người dùng vào danh sách để hiển thị
                        userList.add("Email: " + user.getEmail() + ", Phone: " + user.getPhone() + ", Role: " + user.getRole());
                    }
                }
                adapter.notifyDataSetChanged(); // Cập nhật ListView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserManagementActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}