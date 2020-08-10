package com.igorivkin.blobstorage.exceptions;

public class NoSuchBlobVolumeException extends GenericBlobStorageException {
    public NoSuchBlobVolumeException(String message) {
        super(message);
    }
}
