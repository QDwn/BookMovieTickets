package com.example.movieee.Auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.movieee.Admin.AdminActivity;
import com.example.movieee.Model.HelperClass;
import com.example.movieee.Home.MainActivity2;
import com.example.movieee.R;
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

    private static final int RC_SIGN_IN = 9001;

    EditText loginEmail, loginPassWord;
    Button loginButton;
    TextView signupRedirectText, forgotPasswordText;
    ImageView imageViewGoogle, imageViewFacebook, imageViewInstagram;
    CheckBox rememberMeCheckbox;
    ImageView eyeIconLogin;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private static final String PREFS_NAME = "MyLoginPrefs";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_REMEMBER_ME = "rememberMe";


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
        imageViewFacebook = findViewById(R.id.imageViewFacebook);
        imageViewInstagram = findViewById(R.id.imageViewInstagram);
        rememberMeCheckbox = findViewById(R.id.checkBox2);
        eyeIconLogin = findViewById(R.id.eye_icon_login);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_app_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Tải thông tin đăng nhập đã lưu (nếu có)
        loadSavedLoginDetails();


        final boolean[] isPasswordVisible = {false};

        eyeIconLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible[0]) {
                    // Ẩn
                    loginPassWord.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeIconLogin.setImageResource(R.drawable.ic_eye_off);
                } else {
                    // Hiện
                    loginPassWord.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eyeIconLogin.setImageResource(R.drawable.ic_eye);
                }
                isPasswordVisible[0] = !isPasswordVisible[0];
                loginPassWord.setSelection(loginPassWord.length());
            }
        });


        // Listener cho nút Đăng nhập truyền thống
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateUsername() || !validatePassWord()) {
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


        imageViewGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        imageViewFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Facebook Login is not configured.", Toast.LENGTH_SHORT).show();
            }
        });

        imageViewInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Instagram login is not configured.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("LoginActivity", "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "Google Sign In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


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
                            if (user != null && user.getEmail() != null) {
                                String username = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail();
                                Log.d("LoginActivityDebug", "Saving to SharedPreferences (Google): Email = " + user.getEmail() + ", Username = " + username);
                                saveUserInfoToSharedPreferences(user.getEmail(), username);
                            }
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
            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
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

    // Phương thức để tải thông tin đăng nhập đã lưu
    private void loadSavedLoginDetails() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(PREF_REMEMBER_ME, false);
        if (rememberMe) {
            String username = preferences.getString(PREF_USERNAME, "");
            String password = preferences.getString(PREF_PASSWORD, "");
            loginEmail.setText(username);
            loginPassWord.setText(password);
            rememberMeCheckbox.setChecked(true);
        }
    }

    // Phương thức để lưu thông tin đăng nhập
    private void saveLoginDetails(String username, String password, boolean rememberMe) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        if (rememberMe) {
            editor.putString(PREF_USERNAME, username);
            editor.putString(PREF_PASSWORD, password);
            editor.putBoolean(PREF_REMEMBER_ME, true);
        } else {
            // Xóa thông tin đã lưu nếu người dùng bỏ chọn "Remember Me"
            editor.remove(PREF_USERNAME);
            editor.remove(PREF_PASSWORD);
            editor.putBoolean(PREF_REMEMBER_ME, false); // Vẫn lưu trạng thái checkbox
        }
        editor.apply();
    }

    // Phương thức checkUser
    public void checkUser() {
        String userEmail = loginEmail.getText().toString().trim();
        String userPassword = loginPassWord.getText().toString().trim();
        boolean rememberMe = rememberMeCheckbox.isChecked();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("email").equalTo(userEmail);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    loginEmail.setError(null);
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        HelperClass user = userSnapshot.getValue(HelperClass.class);
                        if (user != null) {
                            String passwordFromDB = user.getPassword();

                            if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                                loginPassWord.setError(null);
                                mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                                        .addOnCompleteListener(LoginActivity.this, task -> {
                                            if (task.isSuccessful()) {
                                                saveLoginDetails(userEmail, userPassword, rememberMe);
                                                saveUserInfoToSharedPreferences(user.getEmail(), user.getUsername());
                                                String role = user.getRole();
                                                if (role != null && role.equals("admin")) {
                                                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                                                    startActivity(intent);
                                                }
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Đăng nhập  thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                return;
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
    private void saveUserInfoToSharedPreferences(String email, String username) {
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user_email", email);
        editor.putString("user_username", username);
        editor.apply();
        Log.d("SharedPreferences", "Email saved: " + email);
    }
}