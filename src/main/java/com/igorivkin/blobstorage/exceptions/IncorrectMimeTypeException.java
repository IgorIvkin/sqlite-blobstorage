package com.igorivkin.blobstorage.exceptions;

public class IncorrectMimeTypeException extends GenericBlobStorageException {
    public IncorrectMimeTypeException(String message) {
        super(message);
    }
}