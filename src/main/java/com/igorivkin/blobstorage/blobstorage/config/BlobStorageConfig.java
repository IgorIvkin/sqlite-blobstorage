package com.igorivkin.blobstorage.blobstorage.config;

import java.util.ArrayList;
import java.util.List;

public class BlobStorageConfig {
    /**
     * Maximal blob volume size in megabytes by default.
     */
    private long maxBlobVolumeSize;

    /**
     * True if it's possible to create new blob volumes
     * when no suitable blob volume found to store the item.
     */
    private boolean allowToCreateNewVolumes;

    /**
     * Maximal blob item size to store the file in megabytes.
     * By default it's 20 megabytes.
     */
    private long maxBlobItemSize;

    /**
     * List of allowed mime-types defined by their string names like
     * "application/json" or "image/jpeg".
     */
    private List<String> allowedMimeTypes;

    public BlobStorageConfig() {
        this.allowedMimeTypes = new ArrayList<>();
    }

    public long getMaxBlobVolumeSize() {
        return maxBlobVolumeSize;
    }

    public void setMaxBlobVolumeSize(long maxBlobVolumeSize) {
        this.maxBlobVolumeSize = maxBlobVolumeSize;
    }

    public long getMaxBlobItemSize() {
        return maxBlobItemSize;
    }

    public void setMaxBlobItemSize(long maxBlobItemSize) {
        this.maxBlobItemSize = maxBlobItemSize;
    }

    public boolean isAllowToCreateNewVolumes() {
        return allowToCreateNewVolumes;
    }

    public void setAllowToCreateNewVolumes(boolean allowToCreateNewVolumes) {
        this.allowToCreateNewVolumes = allowToCreateNewVolumes;
    }

    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }
}
