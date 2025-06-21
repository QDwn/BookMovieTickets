package com.example.movieee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Imports cho Google Sign-In
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001; // Request code cho Google Sign-In

    EditText loginEmail, loginPassWord;
    Button loginButton;
    TextView signupRedirectText, forgotPasswordText;
    ImageView imageViewGoogle, imageViewFacebook, imageViewInstagram; // Giữ lại ImageView cho FB/IG để tương ứng với layout

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        loginEmail = findViewById(R.id.login_email);
        loginPassWord = findViewById(R.id.login_pw);
        loginButton = findViewById(R.id.buttonLogin);
        signupRedirectText = findViewById(R.id.textsignup);
        forgotPasswordText = findViewById(R.id.textForgotPassword);
        imageViewGoogle = findViewById(R.id.imageViewGoogle);
        imageViewFacebook = findViewById(R.id.imageViewFacebook); // Ánh xạ ID
        imageViewInstagram = findViewById(R.id.imageViewInstagram); // Ánh xạ ID

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_app_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // Listener cho nút Đăng nhập truyền thống
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateUsername() || !validatePassWord()) {
                    // Không làm gì cả nếu dữ liệu không hợp lệ
                } else {
                    checkUser();
                }
            }
        });

        // Listener cho nút Đăng ký
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        // Listener cho nút Quên mật khẩu
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Listener cho nút Đăng nhập bằng Google
        imageViewGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        // Listener cho nút Đăng nhập bằng Facebook (chỉ hiển thị Toast)
        imageViewFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Facebook Login is not configured.", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener cho nút Đăng nhập bằng Instagram (chỉ hiển thị Toast)
        imageViewInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Instagram login is not configured.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức xử lý kết quả từ Google Sign-In
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("LoginActivity", "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "Google Sign In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- Google Sign-In Logic ---
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("LoginActivity", "Google sign-in successful with Firebase.");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w("LoginActivity", "Google sign-in failed with Firebase.", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(LoginActivity.this, "Signed in as: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Not signed in.", Toast.LENGTH_SHORT).show();
        }
    }


    public Boolean validateUsername() {
        String val = loginEmail.getText().toString();
        if (val.isEmpty()) {
            loginEmail.setError("Username cannot be empty");
            return false;
        } else {
            loginEmail.setError(null);
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

    // Phương thức checkUser đã được sửa đổi
    public void checkUser() {
        String userEmail = loginEmail.getText().toString().trim(); // Đổi tên biến để rõ ràng hơn
        String userPassword = loginPassWord.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        // Truy vấn dựa trên trường 'email' bên trong các node con
        Query checkUserDatabase = reference.orderByChild("email").equalTo(userEmail);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    loginEmail.setError(null);
                    // Duyệt qua các kết quả (thường chỉ có 1 nếu email là duy nhất)
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        HelperClass user = userSnapshot.getValue(HelperClass.class); // Lấy đối tượng HelperClass
                        if (user != null) {
                            String passwordFromDB = user.getPassword();

                            if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                                loginPassWord.setError(null);
                                // === BẮT ĐẦU THAY ĐỔI QUAN TRỌNG TẠI ĐÂY ===
                                // Đăng nhập người dùng vào Firebase Authentication SAU KHI xác minh trong Realtime Database
                                mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                                        .addOnCompleteListener(LoginActivity.this, task -> {
                                            if (task.isSuccessful()) {
                                                // Đăng nhập Firebase Auth thành công
                                                FirebaseUser firebaseUser = mAuth.getCurrentUser(); // Lấy đối tượng FirebaseUser
                                                String role = user.getRole(); // Lấy vai trò từ Realtime DB

                                                if (role != null && role.equals("admin")) {
                                                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    // Chuyển đến MainActivity2 cho người dùng thông thường
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                                                    startActivity(intent);
                                                }
                                                finish();
                                            } else {
                                                // Đăng nhập Firebase Auth thất bại. Có thể tài khoản chưa được tạo trong Authentication.
                                                // Bạn nên đảm bảo tài khoản được tạo trong Firebase Authentication khi đăng ký.
                                                Toast.makeText(LoginActivity.this, "Đăng nhập Firebase Auth thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                // Tùy chọn: vẫn cho phép vào nếu Realtime DB ok, nhưng không lý tưởng
                                                // Nếu bạn muốn người dùng vẫn vào được ứng dụng ngay cả khi Authentication thất bại (nhưng DB OK),
                                                // hãy di chuyển phần Intent bên dưới ra ngoài khối else này.
                                                // Tuy nhiên, tốt nhất là nên đồng bộ Firebase Auth và Realtime DB.
                                            }
                                        });
                                // === KẾT THÚC THAY ĐỔI QUAN TRỌNG ===
                                return; // Đã tìm thấy người dùng và xử lý, thoát khỏi vòng lặp
                            }
                        }
                    }
                    loginPassWord.setError("Invalid credentials");
                    loginPassWord.requestFocus();
                } else {
                    loginEmail.setError("User does not exist");
                    loginEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}