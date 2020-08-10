package com.igorivkin.blobstorage.blobitem;

import java.util.Objects;

public class BlobStoredItemAddress {
    private long id;
    private int volumeId;

    public BlobStoredItemAddress() {}

    public BlobStoredItemAddress(long id, int volumeId) {
        this.id = id;
        this.volumeId = volumeId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(int volumeId) {
        this.volumeId = volumeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlobStoredItemAddress that = (BlobStoredItemAddress) o;
        return id == that.id &&
                volumeId == that.volumeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, volumeId);
    }
}
