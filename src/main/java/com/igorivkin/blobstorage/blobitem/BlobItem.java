package com.igorivkin.blobstorage.blobitem;

import java.util.Arrays;
import java.util.Objects;

public class BlobItem {
    private long id;
    private String mimeType;
    private int status;
    private byte[] content;

    public BlobItem() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlobItem blobItem = (BlobItem) o;
        return id == blobItem.id &&
                status == blobItem.status &&
                Objects.equals(mimeType, blobItem.mimeType) &&
                Arrays.equals(content, blobItem.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, mimeType, status);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }
}
