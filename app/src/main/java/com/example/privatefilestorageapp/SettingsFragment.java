package com.example.privatefilestorageapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private SharedPreferences sharedPreferences;
    private SwitchMaterial biometricSwitch;
    private MaterialCardView biometricCard;
    private MaterialCardView logoutCard;
    private BiometricAuth biometricAuth;
    private TextView profileName;
    private TextView profileEmail;
    private ShapeableImageView profileImageView;
    private SwitchMaterial openFileBiometricSwitch;
    private SwitchMaterial downloadFileBiometricSwitch;
    private MaterialCardView openFileBiometricCard;
    private MaterialCardView downloadFileBiometricCard;

    // New: ActivityResultLauncher to replace startActivityForResult/onActivityResult
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ActivityResultLauncher - this replaces onActivityResult
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            Log.d(TAG, "Selected image URI: " + imageUri.toString());

                            // Call the method to handle the selected image
                            handleProfileImageResult(imageUri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize BiometricAuth
        biometricAuth = new BiometricAuth(requireContext());

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", requireActivity().MODE_PRIVATE);

        // Initialize views
        biometricCard = view.findViewById(R.id.biometric_card);
        biometricSwitch = view.findViewById(R.id.biometric_switch);
        logoutCard = view.findViewById(R.id.logout_card);

        // Initialize profile views
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profileImageView = view.findViewById(R.id.profile_image);

        // Initialize new switches and cards
        openFileBiometricCard = view.findViewById(R.id.open_file_biometric_card);
        downloadFileBiometricCard = view.findViewById(R.id.download_file_biometric_card);
        openFileBiometricSwitch = view.findViewById(R.id.open_file_biometric_switch);
        downloadFileBiometricSwitch = view.findViewById(R.id.download_file_biometric_switch);

        // Add profile image click listener
        profileImageView.setOnClickListener(v -> handleProfileImageClick());

        // Load user profile
        loadUserProfile();

        // Update profile image
        updateProfileImage();

        // Set up biometric functionality
        setupBiometricOption();

        // Setup new biometric options
        setupFileOperationBiometricOptions();

        // Set up logout functionality
        logoutCard.setOnClickListener(v -> handleLogout());
    }

    private void handleProfileImageClick() {
        // Updated: use the launcher instead of startActivityForResult
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Picture"));
    }

    private void handleProfileImageResult(Uri sourceUri) {
        try {
            // Create a file in your app's internal storage
            String fileName = "profile_image.jpg";
            File destFile = new File(requireContext().getFilesDir(), fileName);

            // Copy the image from the selected URI to your app's storage
            InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
            FileOutputStream outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Save the path to the internal file instead of the external URI
            sharedPreferences.edit()
                    .putString("ProfileImagePath", destFile.getAbsolutePath())
                    .apply();

            // Update the profile image
            updateProfileImage();

        } catch (Exception e) {
            Log.e(TAG, "Error copying profile image: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Failed to save profile image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileImage() {
        // First try to load from internal storage path
        String imagePath = sharedPreferences.getString("ProfileImagePath", null);

        if (imagePath != null) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    // Load image from file
                    profileImageView.setImageTintList(null);
                    profileImageView.setImageURI(Uri.fromFile(imageFile));
                    profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile image from path: " + e.getMessage(), e);
            }
        }

        // Fall back to URI method if path method failed
        String imageUriString = sharedPreferences.getString("ProfileImageUri", null);
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriString);

                // Important: Remove the tint when loading a real image
                profileImageView.setImageTintList(null);

                // Set the image URI
                profileImageView.setImageURI(imageUri);

                // Use centerCrop for better display of user photos
                profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from URI: " + e.getMessage(), e);
                setDefaultProfileImage();
            }
        } else {
            // If no path or URI found, show default
            setDefaultProfileImage();
        }
    }

    private void setDefaultProfileImage() {
        profileImageView.setImageResource(R.drawable.baseline_account_circle_24);
        // Apply tint only for the default icon
        profileImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer)));
        // Use center scaling for the icon
        profileImageView.setScaleType(ImageView.ScaleType.CENTER);
    }

    private void loadUserProfile() {
        // Get current user from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Get user email
            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty()) {
                profileEmail.setText(email);

                // Get display name or extract username from email
                String displayName = currentUser.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    profileName.setText(displayName);
                } else {
                    // Use part of email before @ as display name
                    try {
                        String username = email.substring(0, email.indexOf('@'));
                        // Capitalize first letter for better appearance
                        username = username.substring(0, 1).toUpperCase() + username.substring(1);
                        profileName.setText(username);
                    } catch (Exception e) {
                        // Fallback if email parsing fails
                        profileName.setText("User");
                        Log.e(TAG, "Error parsing email for username: " + e.getMessage());
                    }
                }
            } else {
                // Fallback for no email
                profileName.setText("User");
                profileEmail.setText("No email available");
            }
        } else {
            // User not logged in - shouldn't happen in settings screen
            // but adding as a safeguard
            profileName.setText("Guest");
            profileEmail.setText("Not signed in");
        }
    }

    private void setupBiometricOption() {
        boolean isBiometricAvailable = biometricAuth.isBiometricAvailable();
        boolean isBiometricEnabled = sharedPreferences.getBoolean("BiometricEnabled", false);

        // Show or hide biometric option based on device capability
        if (isBiometricAvailable) {
            biometricCard.setVisibility(View.VISIBLE);
            biometricSwitch.setChecked(isBiometricEnabled);

            // Set up switch listener
            biometricSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Save preference
                    sharedPreferences.edit()
                            .putBoolean("BiometricEnabled", isChecked)
                            .apply();

                    // Show confirmation to user
                    Toast.makeText(
                            requireContext(),
                            isChecked ? "Biometric login enabled" : "Biometric login disabled",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        } else {
            biometricCard.setVisibility(View.GONE);
        }
    }

    private void setupFileOperationBiometricOptions() {
        // Check biometric availability
        boolean isBiometricAvailable = biometricAuth.isBiometricAvailable();

        if (isBiometricAvailable) {
            // Open File Biometric Switch
            boolean isOpenFileBiometricEnabled = sharedPreferences.getBoolean("BiometricOpenFileEnabled", false);
            openFileBiometricCard.setVisibility(View.VISIBLE);
            openFileBiometricSwitch.setChecked(isOpenFileBiometricEnabled);
            openFileBiometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit()
                        .putBoolean("BiometricOpenFileEnabled", isChecked)
                        .apply();

                Toast.makeText(
                        requireContext(),
                        isChecked ? "Biometric file open enabled" : "Biometric file open disabled",
                        Toast.LENGTH_SHORT
                ).show();
            });

            // Download File Biometric Switch
            boolean isDownloadFileBiometricEnabled = sharedPreferences.getBoolean("BiometricDownloadFileEnabled", false);
            downloadFileBiometricCard.setVisibility(View.VISIBLE);
            downloadFileBiometricSwitch.setChecked(isDownloadFileBiometricEnabled);
            downloadFileBiometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit()
                        .putBoolean("BiometricDownloadFileEnabled", isChecked)
                        .apply();

                Toast.makeText(
                        requireContext(),
                        isChecked ? "Biometric file download enabled" : "Biometric file download disabled",
                        Toast.LENGTH_SHORT
                ).show();
            });
        } else {
            // Hide cards if biometrics not available
            openFileBiometricCard.setVisibility(View.GONE);
            downloadFileBiometricCard.setVisibility(View.GONE);
        }
    }

    private void handleLogout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear biometric settings
        sharedPreferences.edit()
                .putBoolean("BiometricEnabled", false)
                .putInt("FailedAttempts", 0)
                .apply();

        // Redirect to login screen
        Intent intent = new Intent(requireContext(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh settings when returning to the fragment
        setupBiometricOption();
        setupFileOperationBiometricOptions();
        // Refresh user profile in case it changed
        loadUserProfile();
        // Refresh profile image
        updateProfileImage();
    }
}