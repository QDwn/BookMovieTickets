package com.example.movieee;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import com.example.movieee.Adapter.UserAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserManagementActivity extends AppCompatActivity {

    RecyclerView usersRecyclerView; // Đổi từ ListView thành RecyclerView
    UserAdapter userAdapter; // Sử dụng UserAdapter mới
    DatabaseReference usersRef;
    ArrayList<HelperClass> userObjectList; // Giữ nguyên danh sách đối tượng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        usersRecyclerView = findViewById(R.id.usersRecyclerView); // Tham chiếu RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Đặt LayoutManager

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        userObjectList = new ArrayList<>();
        userAdapter = new UserAdapter(userObjectList); // Khởi tạo UserAdapter
        usersRecyclerView.setAdapter(userAdapter);

        // Đặt lắng nghe sự kiện nhấp chuột cho Adapter
        userAdapter.setOnItemClickListener(user -> showUserOptionsDialog(user));

        loadUsers();
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userObjectList.clear(); // Xóa cả danh sách đối tượng
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    HelperClass user = userSnapshot.getValue(HelperClass.class);
                    if (user != null) {
                        userObjectList.add(user); // Thêm đối tượng người dùng vào danh sách
                    }
                }
                userAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView thông qua Adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserManagementActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserOptionsDialog(HelperClass user) {
        new AlertDialog.Builder(this, R.style.AlertDialog_RedBlackTheme)
                .setTitle("Tùy chọn người dùng: " + user.getEmail())
                .setItems(new String[]{"Sửa thông tin", "Thay đổi vai trò", "Xóa người dùng"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Sửa thông tin
                            showEditUserDialog(user);
                            break;
                        case 1: // Thay đổi vai trò
                            showChangeRoleDialog(user);
                            break;
                        case 2: // Xóa người dùng
                            showDeleteConfirmationDialog(user);
                            break;
                    }
                })
                .show();
    }

    private void showEditUserDialog(HelperClass user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog_RedBlackTheme);
        builder.setTitle("Sửa thông tin người dùng");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        EditText editPhone = dialogView.findViewById(R.id.edit_user_phone);
        EditText editEmail = dialogView.findViewById(R.id.edit_user_email);
        EditText editPassword = dialogView.findViewById(R.id.edit_user_password);

        editPhone.setText(user.getPhone());
        editEmail.setText(user.getEmail());
        editPassword.setText(user.getPassword());

        builder.setView(dialogView);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newPhone = editPhone.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();
            String newPassword = editPassword.getText().toString().trim();

            String oldEmailKey = "user_" + user.getEmail().replace(".", "_");
            String newEmailKey = "user_" + newEmail.replace(".", "_");

            // Cập nhật đối tượng HelperClass
            user.setPhone(newPhone);
            user.setEmail(newEmail);
            user.setPassword(newPassword);

            if (!oldEmailKey.equals(newEmailKey)) {
                usersRef.child(oldEmailKey).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        usersRef.child(newEmailKey).setValue(user)
                                .addOnSuccessListener(aVoid -> Toast.makeText(UserManagementActivity.this, "Đã cập nhật người dùng và email", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(UserManagementActivity.this, "Lỗi khi cập nhật email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(UserManagementActivity.this, "Lỗi khi xóa người dùng cũ: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                usersRef.child(newEmailKey).setValue(user)
                        .addOnSuccessListener(aVoid -> Toast.makeText(UserManagementActivity.this, "Đã cập nhật người dùng", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(UserManagementActivity.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showChangeRoleDialog(HelperClass user) {
        final String[] roles = {"user", "admin"};
        int currentRoleIndex = 0;
        if (user.getRole() != null && user.getRole().equals("admin")) {
            currentRoleIndex = 1;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog_RedBlackTheme);
        builder.setTitle("Thay đổi vai trò cho " + user.getEmail());
        builder.setSingleChoiceItems(roles, currentRoleIndex, (dialog, which) -> {
            String newRole = roles[which];
            if (!user.getRole().equals(newRole)) {
                String emailKey = "user_" + user.getEmail().replace(".", "_");
                usersRef.child(emailKey).child("role").setValue(newRole)
                        .addOnSuccessListener(aVoid -> Toast.makeText(UserManagementActivity.this, "Đã thay đổi vai trò thành " + newRole, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(UserManagementActivity.this, "Lỗi khi thay đổi vai trò: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }


    private void showDeleteConfirmationDialog(HelperClass user) {
        new AlertDialog.Builder(this, R.style.AlertDialog_RedBlackTheme)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng " + user.getEmail() + " này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String emailKey = "user_" + user.getEmail().replace(".", "_");
                    usersRef.child(emailKey).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(UserManagementActivity.this, "Đã xóa người dùng: " + user.getEmail(), Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(UserManagementActivity.this, "Lỗi khi xóa người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}