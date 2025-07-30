package com.example.privatefilestorageapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email_text;
    private EditText password_text;
    private Button signin_button;
    private Button gotosignup_button;
    private Button biometricLoginButton;
    private BiometricAuth biometricAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        biometricAuth = new BiometricAuth(this);

        email_text = findViewById(R.id.signInEmail);
        password_text = findViewById(R.id.signInPassword);
        signin_button = findViewById(R.id.signInButton);
        gotosignup_button = findViewById(R.id.goToSignUpButton);
        biometricLoginButton = findViewById(R.id.biometricLoginButton);

        // **Check if Biometric Login is Enabled and if failed attempts are below 3**
        boolean isBiometricEnabled = sharedPreferences.getBoolean("BiometricEnabled", false);
        int failedAttempts = sharedPreferences.getInt("FailedAttempts", 0);

        if (!isBiometricEnabled || !biometricAuth.isBiometricAvailable() || failedAttempts >= 3) {
            biometricLoginButton.setVisibility(View.GONE); // Hide biometric button if disabled or too many failed attempts
        } else {
            biometricLoginButton.setVisibility(View.VISIBLE); // Show biometric button
        }

        signin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });

        biometricLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricAuth.showBiometricPrompt();
            }
        });

        // Redirect to Sign-up Page
        gotosignup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void signInUser() {
        String email = email_text.getText().toString().trim();
        String password = password_text.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("SignInActivity", "Sign-in successful");
                            Toast.makeText(SignInActivity.this, "Sign-in Successful!", Toast.LENGTH_SHORT).show();

                            // Reset failed attempts on successful login
                            sharedPreferences.edit().putInt("FailedAttempts", 0).apply();

                            // Redirect to Home Activity
                            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w("SignInActivity", "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
