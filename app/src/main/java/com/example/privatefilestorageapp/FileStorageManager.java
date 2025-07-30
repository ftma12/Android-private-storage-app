package com.example.privatefilestorageapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileStorageManager {

    private static final String PREFS_NAME = "UploadedFiles";
    private static final String FILE_LIST_KEY = "fileList";

    private Context context;

    public FileStorageManager(Context context) {
        this.context = context;
    }

    // Save File to Internal Storage with file type
    public boolean saveFileToAppStorage(Uri fileUri, String fileName, String fileType) {
        if (fileUri == null || fileName.isEmpty()) return false;

        ContentResolver contentResolver = context.getContentResolver();
        File appStorageDir = context.getFilesDir();
        File newFile = new File(appStorageDir, fileName);

        try (ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(fileUri, "r");
             FileInputStream inputStream = new FileInputStream(pfd.getFileDescriptor());
             FileOutputStream outputStream = new FileOutputStream(newFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Save file details with file type
            saveFileDetails(fileName, newFile.getAbsolutePath(), fileType);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Support for old method signature for backward compatibility
    public boolean saveFileToAppStorage(Uri fileUri, String fileName) {
        // Use "Document" as default file type
        return saveFileToAppStorage(fileUri, fileName, "Document");
    }

    // Save file details with file type usinf AI
    private void saveFileDetails(String fileName, String filePath, String fileType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String existingFiles = sharedPreferences.getString(FILE_LIST_KEY, "");
        // Format: Name::Path::Timestamp::FileType
        String newFileEntry = fileName + "::" + filePath + "::" + System.currentTimeMillis() + "::" + fileType;
        String updatedFiles = existingFiles.isEmpty() ? newFileEntry : existingFiles + ";;" + newFileEntry;

        editor.putString(FILE_LIST_KEY, updatedFiles);
        editor.apply();
    }

    // Retrieve List of Saved Files
    public List<FileItemModel> getStoredFiles() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String fileData = sharedPreferences.getString(FILE_LIST_KEY, "");

        List<FileItemModel> files = new ArrayList<>();
        if (!fileData.isEmpty()) {
            String[] fileEntries = fileData.split(";;");
            for (String entry : fileEntries) {
                String[] parts = entry.split("::");
                if (parts.length >= 3) {
                    // Use file type if available, otherwise use "Document" as default
                    String fileType = (parts.length >= 4) ? parts[3] : "Document";
                    files.add(new FileItemModel(parts[0], parts[1], Long.parseLong(parts[2]), fileType));
                }
            }
        }
        return files;
    }

    // Delete File from Storage & SharedPreferences
    public boolean deleteFile(String fileName) {
        File appStorageDir = context.getFilesDir();
        File fileToDelete = new File(appStorageDir, fileName);

        if (fileToDelete.exists() && fileToDelete.delete()) {
            removeFileDetails(fileName);
            return true;
        }
        return false;
    }

    private void removeFileDetails(String fileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String fileData = sharedPreferences.getString(FILE_LIST_KEY, "");
        if (!fileData.isEmpty()) {
            String[] fileEntries = fileData.split(";;");
            StringBuilder updatedFiles = new StringBuilder();
            for (String entry : fileEntries) {
                if (!entry.startsWith(fileName + "::")) { // Keep all except deleted file
                    if (updatedFiles.length() > 0) updatedFiles.append(";;");
                    updatedFiles.append(entry);
                }
            }
            sharedPreferences.edit().putString(FILE_LIST_KEY, updatedFiles.toString()).apply();
        }
    }
}