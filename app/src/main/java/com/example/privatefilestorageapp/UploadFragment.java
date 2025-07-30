package com.example.privatefilestorageapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class UploadFragment extends Fragment {
    private Uri SelectedfileURI;
    private EditText FileNameInput;
    private Button Selectfilebutton;
    private Button Uploadfilebutton;
    private FloatingActionButton openCameraButton;
    private RadioGroup fileTypeRadioGroup;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private FileStorageManager fileStorageManager;

    // Constants for request codes
    private static final int CAMERA_REQUEST_CODE = 102;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileStorageManager = new FileStorageManager(requireContext());

        // Simplified file picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        SelectedfileURI = result.getData().getData();
                        showSnackbar("File selected successfully");
                    }
                }
        );

        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String uriString = result.getData().getStringExtra("capturedImageUri");
                        if (uriString != null) {
                            SelectedfileURI = Uri.parse(uriString);
                            showSnackbar("Photo captured successfully");

                            // Suggest image type for captured photos
                            fileTypeRadioGroup.check(R.id.radioImage);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        // Simplified view binding
        Selectfilebutton = view.findViewById(R.id.selectFileButton);
        FileNameInput = view.findViewById(R.id.et_file_name);
        Uploadfilebutton = view.findViewById(R.id.uploadFileButton);
        fileTypeRadioGroup = view.findViewById(R.id.fileTypeRadioGroup);
        openCameraButton = view.findViewById(R.id.openCameraButton);

        // Simplified click listeners using lambda
        Selectfilebutton.setOnClickListener(v -> openFilePicker());
        Uploadfilebutton.setOnClickListener(v -> uploadFile());
        openCameraButton.setOnClickListener(v -> openCamera());

        return view;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(requireActivity(), CameraActivity.class);
        cameraLauncher.launch(cameraIntent);
    }

    private void uploadFile() {
        if (SelectedfileURI == null) {
            showSnackbar("No file selected.");
            return;
        }

        String userFileName = FileNameInput.getText().toString().trim();
        if (userFileName.isEmpty()) {
            showSnackbar("Please enter a file name.");
            return;
        }

        // Get selected file type
        String fileType = getSelectedFileType();

        boolean success = fileStorageManager.saveFileToAppStorage(SelectedfileURI, userFileName, fileType);
        if (success) {
            // Clear the form after successful upload
            FileNameInput.setText("");
            SelectedfileURI = null;
            showSnackbar("File uploaded successfully!");
        } else {
            showSnackbar("File upload failed.");
        }
    }

    private String getSelectedFileType() {
        int selectedId = fileTypeRadioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.radioImage) {
            return "Image";
        } else if (selectedId == R.id.radioVideo) {
            return "Video";
        } else if (selectedId == R.id.radioAudio) {
            return "Audio";
        } else {
            return "Document"; // Default
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.md_theme_primaryContainer))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer))
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_secondary))
                .setAction("OK", v -> {})
                .show();
    }
}