package com.g.media.uploader.exception;

public class UploaderException extends RuntimeException{
    public UploaderException(String message) {
        super(message);
    }

    public UploaderException(Throwable cause) {
        super(cause);
    }

    public UploaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
