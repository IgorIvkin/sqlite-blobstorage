package com.igorivkin.blobstorage.exceptions;

public class TooBigItemException extends GenericBlobStorageException {
    public TooBigItemException(String message) {
        super(message);
    }
}
