package com.big.dreamer.doccentral.storage;

public class StorageBackupException extends RuntimeException {
    public StorageBackupException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageBackupException(String message) {
        super(message);
    }
}
