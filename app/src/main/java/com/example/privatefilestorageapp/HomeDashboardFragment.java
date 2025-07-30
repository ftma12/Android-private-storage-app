package com.example.privatefilestorageapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class HomeDashboardFragment extends Fragment {
    // Storage constants
    private static final long MAX_STORAGE_BYTES = 1024L * 1024L * 1024L; // 1 GB

    // UI Components
    private TextView documentCountText;
    private TextView imageCountText;
    private TextView videoCountText;
    private TextView audioCountText;
    private TextView greetingText;
    private TextView storageText;
    private LinearProgressIndicator storageProgress;

    // Card Views for animation
    private MaterialCardView documentCard;
    private MaterialCardView imageCard;
    private MaterialCardView videoCard;
    private MaterialCardView audioCard;

    // Managers
    private FileStorageManager fileStorageManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_home_dashboard, container, false);

        // Initialize FileStorageManager
        fileStorageManager = new FileStorageManager(requireContext());

        // Initialize UI Components
        initializeUIComponents(view);

        // Set up and apply animations
        setupAnimations();

        // Set up click listeners for cards
        setupCardClickListeners();

        // Update dashboard
        updateDashboard();

        return view;
    }

    private void initializeUIComponents(View view) {
        // Text Views
        documentCountText = view.findViewById(R.id.document_count);
        imageCountText = view.findViewById(R.id.image_count);
        videoCountText = view.findViewById(R.id.video_count);
        audioCountText = view.findViewById(R.id.audio_count);
        greetingText = view.findViewById(R.id.greeting_text);
        storageText = view.findViewById(R.id.storage_text);
        storageProgress = view.findViewById(R.id.storage_progress);

        // Card Views
        documentCard = view.findViewById(R.id.document_card);
        imageCard = view.findViewById(R.id.image_card);
        videoCard = view.findViewById(R.id.video_card);
        audioCard = view.findViewById(R.id.audio_card);

        // Set greeting text
        setGreeting();
    }

    private void setGreeting() {
        greetingText.setText("Hello!");
    }

    private void setupAnimations() {
        // Animate greeting
        Animation greetingAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);
        greetingText.startAnimation(greetingAnimation);

        // Animate cards with staggered entrance
        Animation documentCardAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);
        documentCardAnimation.setStartOffset(100);
        documentCard.startAnimation(documentCardAnimation);

        Animation imageCardAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);
        imageCardAnimation.setStartOffset(200);
        imageCard.startAnimation(imageCardAnimation);

        Animation videoCardAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);
        videoCardAnimation.setStartOffset(300);
        videoCard.startAnimation(videoCardAnimation);

        Animation audioCardAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);
        audioCardAnimation.setStartOffset(400);
        audioCard.startAnimation(audioCardAnimation);
    }

    private void setupCardClickListeners() {
        // Add click listeners to each card to navigate to FilesFragment with proper filter
        documentCard.setOnClickListener(v -> navigateToFilesWithFilter("Document"));
        imageCard.setOnClickListener(v -> navigateToFilesWithFilter("Image"));
        videoCard.setOnClickListener(v -> navigateToFilesWithFilter("Video"));
        audioCard.setOnClickListener(v -> navigateToFilesWithFilter("Audio"));
    }

    private void navigateToFilesWithFilter(String fileType) {
        // Create new FilesFragment instance
        FilesFragment filesFragment = new FilesFragment();

        // Create bundle with filter parameter
        Bundle args = new Bundle();
        args.putString("fileTypeFilter", fileType);
        filesFragment.setArguments(args);

        // Navigate to the fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, filesFragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateDashboard() {
        // Get stored files
        List<FileItemModel> storedFiles = fileStorageManager.getStoredFiles();

        // Counters for file types
        int documentCount = 0;
        int imageCount = 0;
        int videoCount = 0;
        int audioCount = 0;

        // Total storage calculation
        long totalStorageUsed = 0;

        // Get internal storage directory
        File storageDir = requireContext().getFilesDir();

        // Get list of all files
        File[] files = storageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                totalStorageUsed += file.length();
            }
        }

        // Count files by type
        for (FileItemModel file : storedFiles) {
            switch (file.getFileType()) {
                case "Document":
                    documentCount++;
                    break;
                case "Image":
                    imageCount++;
                    break;
                case "Video":
                    videoCount++;
                    break;
                case "Audio":
                    audioCount++;
                    break;
            }
        }

        // Update UI with file counts
        documentCountText.setText(String.format(Locale.getDefault(),
                "%d Documents", documentCount));
        imageCountText.setText(String.format(Locale.getDefault(),
                "%d Images", imageCount));
        videoCountText.setText(String.format(Locale.getDefault(),
                "%d Videos", videoCount));
        audioCountText.setText(String.format(Locale.getDefault(),
                "%d Audio", audioCount));

        // Calculate and display storage usage
        updateStorageUsage(totalStorageUsed);
    }
//used the AI
    private void updateStorageUsage(long usedBytes) {
        // Convert to MB
        double usedMB = usedBytes / (1024.0 * 1024.0);
        double totalMB = MAX_STORAGE_BYTES / (1024.0 * 1024.0);

        // Format to one decimal place
        DecimalFormat df = new DecimalFormat("#.#");
        String storageUsageText = df.format(usedMB) + " MB / " +
                df.format(totalMB) + " MB";
        storageText.setText(storageUsageText);

        // Calculate and set progress
        int progressPercentage = (int) ((usedBytes * 100) / MAX_STORAGE_BYTES);
        storageProgress.setProgress(Math.min(progressPercentage, 100));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh dashboard when fragment becomes visible
        updateDashboard();
    }
}