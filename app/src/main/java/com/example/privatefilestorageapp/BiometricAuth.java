package com.example.privatefilestorageapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricAuth {

    // Define callback interface for file operations
    public interface FileOperationCallback {
        void onSuccess();
        void onFailure(CharSequence errorMessage);
    }

    private final Context context;
    private final BiometricPrompt biometricPrompt;
    private final BiometricPrompt.PromptInfo promptInfo;
    private final SharedPreferences sharedPreferences;

    // Add a flag to control navigation behavior
    private boolean navigateToHome = true;

    // Add a callback field for file operations
    private FileOperationCallback fileCallback = null;

    public BiometricAuth(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        Executor executor = ContextCompat.getMainExecutor(context);
        FragmentActivity activity = (FragmentActivity) context;

        biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                incrementFailedAttempts();
                Toast.makeText(context, "ERROR: " + errString, Toast.LENGTH_LONG).show();

                // Notify callback about failure if it exists
                if (fileCallback != null && !navigateToHome) {
                    fileCallback.onFailure(errString);
                    fileCallback = null; // Clear callback after use
                }
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                resetFailedAttempts(); // Reset the failed attempts on success
                Toast.makeText(context, "Biometric  Successful!", Toast.LENGTH_LONG).show();

                // Only navigate to HomeActivity if flag is set
                if (navigateToHome) {
                    // Redirect to HomeActivity
                    Intent homeIntent = new Intent(context, HomeActivity.class);
                    context.startActivity(homeIntent);
                    ((Activity) context).finish();
                } else if (fileCallback != null) {
                    // For file operations, call the success callback
                    fileCallback.onSuccess();
                    fileCallback = null; // Clear callback after use
                }

                // Reset the flag for next use
                navigateToHome = true;
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                incrementFailedAttempts();
                Toast.makeText(context, "UNSUCCESSFUL: Biometric check failed!", Toast.LENGTH_LONG).show();

                // For file operations, just show the toast but don't call callback yet
                // We'll let the user retry until they hit cancel or succeed
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Use your fingerprint to authenticate")
                .setNegativeButtonText("Cancel")
                .build();
    }

    public boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(context);
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    // Default method for login - will navigate to home after success
    public void showBiometricPrompt() {
        if (getFailedAttempts() >= 3) {
            Toast.makeText(context, "Too many failed attempts. Use manual login.", Toast.LENGTH_LONG).show();
            return;
        }
        navigateToHome = true;
        fileCallback = null; // Ensure no callback is set for login
        biometricPrompt.authenticate(promptInfo);
    }

    // New method for file operations - won't navigate to home and will use callback
    public void showBiometricPromptWithoutNavigation(FileOperationCallback callback) {
        if (getFailedAttempts() >= 3) {
            Toast.makeText(context, "Too many failed attempts. Use manual login.", Toast.LENGTH_LONG).show();
            if (callback != null) {
                callback.onFailure("Too many failed attempts");
            }
            return;
        }
        navigateToHome = false;
        fileCallback = callback;
        biometricPrompt.authenticate(promptInfo);
    }

    // Keep old method for backward compatibility
    public void showBiometricPromptWithoutNavigation() {
        showBiometricPromptWithoutNavigation(null);
    }

    private void incrementFailedAttempts() {
        int failedAttempts = getFailedAttempts() + 1;
        sharedPreferences.edit().putInt("FailedAttempts", failedAttempts).apply();

        if (failedAttempts >= 3) {
            Toast.makeText(context, "Too many failed attempts. Biometric disabled.", Toast.LENGTH_LONG).show();
            sharedPreferences.edit().putBoolean("BiometricEnabled", false).apply(); // Disable biometric login
        }
    }

    private void resetFailedAttempts() {
        sharedPreferences.edit().putInt("FailedAttempts", 0).apply();
    }

    private int getFailedAttempts() {
        return sharedPreferences.getInt("FailedAttempts", 0);
    }
}