package com.snehashis.litext;

import android.net.Uri;

public class RecentFile {

    private String fileName, filePath, fileType;

    public Uri getFileUri() {
        return fileUri;
    }

    Uri fileUri;

    public RecentFile(Uri fileUri) {
        this.fileUri = fileUri;
        this.filePath = fileUri.getLastPathSegment();
        assert this.filePath != null;
        this.filePath = "/" + this.filePath.substring(this.filePath.lastIndexOf(':') + 1);
        this.fileName = this.filePath.substring(this.filePath.lastIndexOf('/') + 1);
        this.fileType = this.fileName.substring(this.fileName.lastIndexOf('.') + 1);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
