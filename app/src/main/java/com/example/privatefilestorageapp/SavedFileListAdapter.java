package com.example.privatefilestorageapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.biometric.BiometricPrompt;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SavedFileListAdapter extends BaseAdapter {

    private Context context;
    private List<SavedFileListModel> fileList;
    private LayoutInflater inflater;
    private SharedPreferences sharedPreferences;
    private BiometricAuth biometricAuth;

    // Store current file paths for biometric operations
    private String currentFilePath;
    private String currentFileType;
    private boolean isCurrentDownloadOperation;

    public SavedFileListAdapter(Context context, List<SavedFileListModel> fileList) {
        this.context = context;
        this.fileList = fileList;
        this.inflater = LayoutInflater.from(context);
        this.sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        this.biometricAuth = new BiometricAuth(context);
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.savedfileitem_list, parent, false);

            holder = new ViewHolder();
            holder.thumbnail = convertView.findViewById(R.id.file_thumbnail);
            holder.fileName = convertView.findViewById(R.id.file_name);
            holder.fileDetails = convertView.findViewById(R.id.file_details);
            holder.moreOptionsButton = convertView.findViewById(R.id.more_options_button);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get current file
        final SavedFileListModel fileItem = fileList.get(position);

        // Set file name
        holder.fileName.setText(fileItem.getFileName());

        // Format and set file details
        String fileSize = formatFileSize(fileItem.getFileSize());
        String formattedDate = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(fileItem.getTimestamp());
        String fileDetailsText = formattedDate + ", " + fileSize;
        holder.fileDetails.setText(fileDetailsText);

        // Set the appropriate icon based on file type
        String fileType = fileItem.getFileType();
        switch (fileType) {
            case "Image":
                holder.thumbnail.setImageResource(R.drawable.image_icon);
                break;
            case "Audio":
                holder.thumbnail.setImageResource(R.drawable.audio_icon);
                break;
            case "Video":
                holder.thumbnail.setImageResource(R.drawable.video_icon);
                break;
            case "Document":
                holder.thumbnail.setImageResource(R.drawable.pdf_icon);
                break;
            default:
                holder.thumbnail.setImageResource(R.drawable.file_placeholder);
                break;
        }

        // Set up the actions button
        holder.moreOptionsButton.setOnClickListener(v -> showFileActionsMenu(v, fileItem));

        return convertView;
    }

    // ViewHolder pattern for efficiency
    private static class ViewHolder {
        ImageView thumbnail;
        TextView fileName;
        TextView fileDetails;
        MaterialButton moreOptionsButton;
    }

    // Show popup menu with file actions
    private void showFileActionsMenu(View view, SavedFileListModel fileItem) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.file_menu);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_open) {
                openFile(fileItem.getFilePath(), fileItem.getFileType());
                return true;
            } else if (itemId == R.id.action_download) {
                downloadFile(fileItem.getFilePath());
                return true;
            } else if (itemId == R.id.action_delete) {
                confirmAndDeleteFile(fileItem);
                return true;
            }
            return false;
        });

        popup.show();
    }

    // Confirm and delete file
    private void confirmAndDeleteFile(SavedFileListModel fileItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete File")
                .setMessage("Are you sure you want to delete " + fileItem.getFileName() + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete the file
                        FileStorageManager fileManager = new FileStorageManager(context);
                        boolean success = fileManager.deleteFile(fileItem.getFileName());

                        if (success) {
                            // Remove from list and update adapter
                            fileList.remove(fileItem);
                            notifyDataSetChanged();
                            Toast.makeText(context, "File deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Format file size in KB or MB
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return (size / 1024) + " KB";
        } else {
            return (size / (1024 * 1024)) + " MB";
        }
    }

    // Updated openFile method
    private void openFile(String filePath, String fileType) {
        // Check if biometric for opening files is enabled
        boolean isBiometricOpenEnabled = sharedPreferences.getBoolean("BiometricOpenFileEnabled", false);

        if (isBiometricOpenEnabled && biometricAuth.isBiometricAvailable()) {
            // Store the file details to be opened after authentication
            currentFilePath = filePath;
            currentFileType = fileType;
            isCurrentDownloadOperation = false;

            // Create callback for authentication result
            BiometricAuth.FileOperationCallback callback = new BiometricAuth.FileOperationCallback() {
                @Override
                public void onSuccess() {
                    // Only proceed with opening file on successful authentication
                    proceedToOpenFile(currentFilePath, currentFileType);
                }

                @Override
                public void onFailure(CharSequence errorMessage) {
                    // Show error message but don't open file
                    Toast.makeText(context,
                            "Authentication required to open file: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                }
            };

            // Use the new method with callback
            try {
                // Try to use the new method with callback
                biometricAuth.showBiometricPromptWithoutNavigation(callback);
            } catch (NoSuchMethodError e) {
                // Fallback to old method - shouldn't happen if BiometricAuth is updated
                Toast.makeText(context, "Please update the app for better security", Toast.LENGTH_SHORT).show();
                proceedToOpenFile(filePath, fileType);
            }
        } else {
            // If biometric is not enabled or not available, open file directly
            proceedToOpenFile(filePath, fileType);
        }
    }

    private void downloadFile(String filePath) {
        // Check if biometric for downloading files is enabled
        boolean isBiometricDownloadEnabled = sharedPreferences.getBoolean("BiometricDownloadFileEnabled", false);

        if (isBiometricDownloadEnabled && biometricAuth.isBiometricAvailable()) {
            // Store the file details to be downloaded after authentication
            currentFilePath = filePath;
            isCurrentDownloadOperation = true;

            // Create callback for authentication result
            BiometricAuth.FileOperationCallback callback = new BiometricAuth.FileOperationCallback() {
                @Override
                public void onSuccess() {
                    // Only proceed with downloading file on successful authentication
                    proceedToDownloadFile(currentFilePath);
                }

                @Override
                public void onFailure(CharSequence errorMessage) {
                    // Show error message but don't download file
                    Toast.makeText(context,
                            "Authentication required to download file: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                }
            };

            // Use the new method with callback
            try {
                // Try to use the new method with callback
                biometricAuth.showBiometricPromptWithoutNavigation(callback);
            } catch (NoSuchMethodError e) {
                // Fallback to old method - shouldn't happen if BiometricAuth is updated
                Toast.makeText(context, "Please update the app for better security", Toast.LENGTH_SHORT).show();
                proceedToDownloadFile(filePath);
            }
        } else {
            // If biometric is not enabled or not available, download file directly
            proceedToDownloadFile(filePath);
        }
    }

// Remove the listenForAuthSuccess method as it's no longer needed

    // Listen for authentication success
    private void listenForAuthSuccess() {
        // Create a simple timer to check and continue the operation
        new Thread(() -> {
            try {
                // Wait for authentication to complete
                Thread.sleep(1500);

                // Run on UI thread
                ((Activity)context).runOnUiThread(() -> {
                    // If we have pending file operations, execute them
                    if (currentFilePath != null) {
                        if (isCurrentDownloadOperation) {
                            proceedToDownloadFile(currentFilePath);
                        } else {
                            proceedToOpenFile(currentFilePath, currentFileType);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void proceedToOpenFile(String filePath, String fileType) {
        try {
            File file = new File(filePath);

            // Additional checks
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
                return;
            }

            if (file.length() == 0) {
                Toast.makeText(context, "File is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);

            // Set MIME type based on file type
            switch (fileType) {
                case "Image":
                    intent.setDataAndType(uri, "image/*");
                    break;
                case "Audio":
                    intent.setDataAndType(uri, "audio/*");
                    break;
                case "Video":
                    intent.setDataAndType(uri, "video/*");
                    break;
                case "Document":
                    intent.setDataAndType(uri, "application/pdf");
                    break;
                default:
                    intent.setDataAndType(uri, "*/*");
                    break;
            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Open file"));

        } catch (Exception e) {
            Log.e("FileOpening", "Error opening file", e);
            Toast.makeText(context, "Unable to open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
//AI
    private void proceedToDownloadFile(String filePath) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, getMimeType(filePath));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Download"));
    }
    private String getMimeType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            default:
                return "*/*";
        }
    }
    // Update list when files are added or deleted
    public void updateFileList(List<SavedFileListModel> updatedList) {
        this.fileList = updatedList;
        notifyDataSetChanged();
    }
}