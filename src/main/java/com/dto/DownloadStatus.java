package com.dto;

public class DownloadStatus {

    private final String message;
    private final boolean status;

    public DownloadStatus(String message, boolean status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "DownloadStatus{" +
                "message='" + message + '\'' +
                ", status=" + status +
                '}';
    }

}
