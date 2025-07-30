package com.example.privatefilestorageapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FilesFragment extends Fragment {

    private ListView fileListView;
    private EditText searchEditText;
    private SavedFileListAdapter fileListAdapter;
    private List<SavedFileListModel> fileList;
    private List<SavedFileListModel> filteredFileList;
    private FileStorageManager fileStorageManager;
    private String fileTypeFilter; // Store the file type filter
    private TextView noFilesMessage; // To show when no matching files found
    private Chip filterChip; // To display active filter

    public FilesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        // Initialize FileStorageManager
        fileStorageManager = new FileStorageManager(requireContext());

        // Check if we have a file type filter from arguments
        if (getArguments() != null) {
            fileTypeFilter = getArguments().getString("fileTypeFilter", null);
        }

        // Initialize ListView
        fileListView = view.findViewById(R.id.file_list_view);

        // Initialize no files message text view
        noFilesMessage = view.findViewById(R.id.no_files_message);
        if (noFilesMessage == null) {
            // If the view doesn't exist in the layout, create it programmatically
            noFilesMessage = new TextView(requireContext());
            noFilesMessage.setText("No files found");
            noFilesMessage.setVisibility(View.GONE);
            ((ViewGroup) fileListView.getParent()).addView(noFilesMessage);
        }

        // Initialize Search EditText
        searchEditText = view.findViewById(R.id.search_edit_text);
        setupSearch();

        // Setup filter chip if we have a filter
        setupFilterChip(view);

        // Load stored files
        fileList = loadStoredFiles();
        filteredFileList = new ArrayList<>();

        // Apply filters (both search and file type)
        applyFilters(searchEditText.getText().toString());

        // Set up the adapter
        fileListAdapter = new SavedFileListAdapter(requireContext(), filteredFileList);
        fileListView.setAdapter(fileListAdapter);

        return view;
    }
//ai
    private void setupFilterChip(View view) {
        if (fileTypeFilter != null && !fileTypeFilter.isEmpty()) {
            filterChip = view.findViewById(R.id.filter_chip);
            if (filterChip != null) {
                filterChip.setVisibility(View.VISIBLE);
                filterChip.setText(fileTypeFilter + " Files");

                // When the chip's close icon is clicked, clear the filter
                filterChip.setOnCloseIconClickListener(v -> {
                    fileTypeFilter = null;
                    filterChip.setVisibility(View.GONE);
                    applyFilters(searchEditText.getText().toString());
                });
            }
        }
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void applyFilters(String searchQuery) {
        filteredFileList.clear();

        // Apply both search and type filters together
        for (SavedFileListModel file : fileList) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    file.getFileName().toLowerCase().contains(searchQuery.toLowerCase());
            boolean matchesType = fileTypeFilter == null ||
                    file.getFileType().equals(fileTypeFilter);

            if (matchesSearch && matchesType) {
                filteredFileList.add(file);
            }
        }

        // Show/hide the no files message
        if (filteredFileList.isEmpty()) {
            noFilesMessage.setVisibility(View.VISIBLE);
            fileListView.setVisibility(View.GONE);
        } else {
            noFilesMessage.setVisibility(View.GONE);
            fileListView.setVisibility(View.VISIBLE);
        }

        // Update adapter
        if (fileListAdapter != null) {
            fileListAdapter.updateFileList(filteredFileList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh file list when user navigates back
        fileList = loadStoredFiles();

        // Reapply any existing filters
        applyFilters(searchEditText.getText().toString());
    }

    // Method to retrieve stored files
    private List<SavedFileListModel> loadStoredFiles() {
        List<SavedFileListModel> files = new ArrayList<>();

        // Get files with their metadata from FileStorageManager
        List<FileItemModel> storedFileDetails = fileStorageManager.getStoredFiles();

        // Get internal storage directory to check file existence and get sizes
        File storageDir = requireContext().getFilesDir();
        File[] storedFiles = storageDir.listFiles();

        if (storedFiles != null) {
            for (File file : storedFiles) {
                String fileName = file.getName();
                String fileType = "Document"; // Default

                // Find the file type from stored metadata
                for (FileItemModel item : storedFileDetails) {
                    if (item.getFileName().equals(fileName)) {
                        fileType = item.getFileType();
                        break;
                    }
                }

                files.add(new SavedFileListModel(
                        fileName,
                        file.getAbsolutePath(),
                        file.length(),
                        file.lastModified(),
                        fileType
                ));
            }
        }

        // Sort files by latest uploaded first
        Collections.sort(files, new Comparator<SavedFileListModel>() {
            @Override
            public int compare(SavedFileListModel f1, SavedFileListModel f2) {
                return Long.compare(f2.getTimestamp(), f1.getTimestamp());
            }
        });

        return files;
    }
}