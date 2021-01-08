package com.igorivkin.blobstorage.blobstorage.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.igorivkin.blobstorage.blobvolume.BlobVolume;
import com.igorivkin.blobstorage.exceptions.GenericBlobStorageException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Component
public class BlobStorageConfigProvider {

    @Value("${blobstorage.configpath}")
    private String blobStorageConfigPath;

    @Value("${blobstorage.databasepath}")
    private String databasePath;

    /**
     * Generic config that is initialized from the file config/blobstorage.json.
     */
    private BlobStorageConfig blobStorageConfig;

    /**
     * List of database volumes presented by files.
     */
    private List<File> databaseVolumes;

    /**
     * Map of the blob volumes presented by their corresponding BlobVolume-objects.
     */
    private Map<String, BlobVolume> blobVolumes;

    private final ObjectProvider<BlobVolume> blobVolumeProvider;

    public BlobStorageConfigProvider(ObjectProvider<BlobVolume> blobVolumeProvider) {
        this.blobVolumeProvider = blobVolumeProvider;
    }

    @PostConstruct
    public void initialize() throws IOException, GenericBlobStorageException {
        this.databaseVolumes = new ArrayList<>();
        this.blobVolumes = new HashMap<>();

        initializeBlobStorageConfig();
        initializeBlobVolumes();
    }

    /**
     * Returns blob volume provider allowing to create blob volume object
     * with all the required dependencies.
     * @return blob volume object provider
     */
    public ObjectProvider<BlobVolume> getBlobVolumeProvider() {
        return this.blobVolumeProvider;
    }

    /**
     * Returns the maximal size of blob volume in bytes.
     * @return maximal blob volume size in bytes
     * @throws GenericBlobStorageException it will crush if no blob storage config is defined
     */
    public long getMaxBlobVolumeSizeInBytes() throws GenericBlobStorageException {
        if (this.blobStorageConfig == null) {
            throw new GenericBlobStorageException("Blob volume config is not defined, cannot get volume size");
        }
        return this.blobStorageConfig.getMaxBlobVolumeSize() * 1024 * 1024;
    }

    /**
     * Returns the maximal size of stored blob item in bytes.
     * @return maximal blob item size in bytes
     * @throws GenericBlobStorageException it will crush if no blob storage config is defined
     */
    public long getMaxBlobItemSize() throws GenericBlobStorageException {
        if (this.blobStorageConfig == null) {
            throw new GenericBlobStorageException("Blob volume config is not defined, cannot get volume size");
        }
        return this.blobStorageConfig.getMaxBlobItemSize() * 1024 * 1024;
    }

    /**
     * Returns currently available database volumes. They are presented by their files.
     * @return list of currently available database volumes
     */
    public List<File> getDatabaseVolumes() {
        return databaseVolumes;
    }

    /**
     * Returns currently available blob volumes. They are presented
     * by their corresponding objects of type BlobVolume.
     * @return list of currently available blob volumes
     */
    public Map<String, BlobVolume> getBlobVolumes() {
        return blobVolumes;
    }

    /**
     * Appends a new database volume file to a list of database volume files.
     * @param databaseVolume file of database volume
     */
    public void appendToDatabaseVolumes(File databaseVolume) {
        this.databaseVolumes.add(databaseVolume);
    }

    /**
     * Returns true if it's possible to create new blob volumes.
     * @return true if allowed to create new blob volumes.
     */
    public boolean isAllowToCreateNewVolume() {
        return this.blobStorageConfig.isAllowToCreateNewVolumes();
    }

    /**
     * Returns list of allowed mime-types.
     * @return list of strings representing allowed mime-types.
     */
    public List<String> getAllowedMimeTypes() {
        return this.blobStorageConfig.getAllowedMimeTypes();
    }

    /**
     * Initializes blob storage config - it contains configuration things
     * for entire blob storage application.
     * @throws IOException it attempts to open json-file so exception is possible
     */
    private void initializeBlobStorageConfig() throws IOException {
        Gson jsonConfig = new Gson();
        try(JsonReader jsonReader = new JsonReader(new FileReader(this.blobStorageConfigPath))) {
            this.blobStorageConfig = jsonConfig.fromJson(jsonReader, BlobStorageConfig.class);
        }
    }

    /**
     * Initializes blob volumes list.
     * @throws GenericBlobStorageException it will crush in case if not possible to get list of files
     */
    private void initializeBlobVolumes() throws GenericBlobStorageException {
        // First we initialize file-defined blob volumes...
        File databaseDirectory = new File(this.databasePath);
        File[] databaseFiles = databaseDirectory.listFiles((dir, name) -> name.startsWith("blob_volume_") && name.endsWith(".db"));
        if(databaseFiles != null) {
            this.databaseVolumes = new ArrayList<>(Arrays.asList(databaseFiles));
        } else {
            throw new GenericBlobStorageException("Cannot get database files, probably path is defined incorrectly");
        }

        // ... and then we initialize blob volumes list with corresponding objects
        for(File databaseVolume: this.getDatabaseVolumes()) {
            BlobVolume blobVolume = this.getBlobVolumeProvider().getObject();
            blobVolume.setVolumeFile(databaseVolume);
            blobVolume.setVolumeName(databaseVolume.getName());
            blobVolume.setVolumeId(this.extractVolumeIdFromVolumeName(blobVolume.getVolumeName()));
            this.blobVolumes.put(blobVolume.getVolumeName(), blobVolume);
        }
    }

    /**
     * Returns a numeric ID of volume taken from its string name.
     * @param volumeName string name of volume
     * @return numeric ID of volume
     */
    private int extractVolumeIdFromVolumeName(String volumeName) {
        return Integer.parseInt(volumeName
                .replace("blob_volume_", "")
                .replace(".db", ""));
    }
}
