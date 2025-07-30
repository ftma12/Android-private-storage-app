package com.example.privatefilestorageapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileItemModel {
    private String fileName;
    private String filePath;
    private long uploadTimestamp;
    private String fileType; // Changed from mimeType to fileType

    public FileItemModel(String fileName, String filePath, long uploadTimestamp) {
        this(fileName, filePath, uploadTimestamp, "Document"); // Default file type is Document
    }

    public FileItemModel(String fileName, String filePath, long uploadTimestamp, String fileType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadTimestamp = uploadTimestamp;
        this.fileType = fileType;
    }

    // Get file name
    public String getFileName() {
        return fileName;
    }

    // Get file path (internal storage location)
    public String getFilePath() {
        return filePath;
    }

    //  Get timestamp (in milliseconds)
    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    //  Get formatted date for UI display
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(new Date(uploadTimestamp));
    }

    // Get file type
    public String getFileType() {
        return fileType;
    }
}