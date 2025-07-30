package com.example.privatefilestorageapp;

public class SavedFileListModel {
    private String fileName;
    private String filePath;
    private long fileSize;
    private long timestamp;
    private String fileType; // Changed from mimeType to fileType

    public SavedFileListModel(String fileName, String filePath, long fileSize, long timestamp) {
        this(fileName, filePath, fileSize, timestamp, "Document"); // Default file type
    }

    public SavedFileListModel(String fileName, String filePath, long fileSize, long timestamp, String fileType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.timestamp = timestamp;
        this.fileType = fileType;
    }

    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public long getFileSize() { return fileSize; }
    public long getTimestamp() { return timestamp; }
    public String getFileType() { return fileType; }
}
