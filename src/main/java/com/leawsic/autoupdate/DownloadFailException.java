package com.leawsic.autoupdate;

public class DownloadFailException extends RuntimeException {
    public DownloadFailException(String message) {
        super(message);
    }
}
