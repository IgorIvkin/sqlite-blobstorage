package com.igorivkin.blobstorage.blobstorage;

import com.igorivkin.blobstorage.blobitem.BlobItem;
import com.igorivkin.blobstorage.blobitem.BlobItemStatus;
import com.igorivkin.blobstorage.blobitem.BlobStoredItemAddress;
import com.igorivkin.blobstorage.blobvolume.BlobVolume;
import com.igorivkin.blobstorage.blobstorage.config.BlobStorageConfigProvider;
import com.igorivkin.blobstorage.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Optional;

@Service
public class BlobStorageImpl implements BlobStorage {
    private final BlobStorageConfigProvider configProvider;

    @Autowired
    public BlobStorageImpl(BlobStorageConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * Stores a given file (represented by InputStream to the blob storage
     * choosing a suitable blob volume for that operation.
     *
     * @param itemBinaryStream input stream representing content of a given file
     * @param mimeType         mime-type of a given file
     * @return ID of inserted entity
     * @throws IOException                 it attempts to analyze file system
     * @throws GenericBlobStorageException it checks for the incoming params and available blob volumes
     * @throws SQLException                it attempts to insert data to sqlite database
     */
    @Override
    public BlobStoredItemAddress storeItem(InputStream itemBinaryStream, String mimeType)
            throws IOException, GenericBlobStorageException, SQLException {

        this.checkMimeType(mimeType);

        // Now prepare the item to store
        BlobItem itemToStore = new BlobItem();
        itemToStore.setMimeType(mimeType);
        itemToStore.setStatus(BlobItemStatus.COMMITTED);
        itemToStore.setContent(itemBinaryStream.readAllBytes());

        // It seems to be inefficient to prepare the item object before to check for the size but actually there is no
        // reliable way to know the size of stream than to read all of it. Method .available() is very unreliable and
        // officially not recommended to use in such a context.
        int sizeOfItem = itemToStore.getContent().length;
        this.checkBlobItemSize(sizeOfItem);

        BlobVolume suitableBlobVolume = this.getSuitableBlobVolume(sizeOfItem);
        if (suitableBlobVolume == null) {
            synchronized (this) {
                // Check again for the suitable blob volumes because it is possible
                // that many threads at the same time were going to this section
                // and somebody has already created the new blob volume
                suitableBlobVolume = this.getSuitableBlobVolume(sizeOfItem);
                if (suitableBlobVolume == null) {
                    // If we found no suitable blob volume that we will try to create one
                    // but only if we are allowed to do that (regulates in blobstorage.json config)
                    if (this.configProvider.isAllowToCreateNewVolume()) {
                        suitableBlobVolume = createNewBlobVolume();
                    } else {
                        throw new GenericBlobStorageException("No blob volumes are available to store the item");
                    }
                }
            }
        }

        // Now we are able to insert the item finally
        return suitableBlobVolume.insert(itemToStore);
    }

    /**
     * Returns a blob item by its given volume ID and ID inside volume.
     *
     * @param id       ID of entity
     * @param idVolume ID of volume
     * @return blob item
     * @throws GenericBlobStorageException it is possible that there will be no such blob volume
     * @throws SQLException                it attempts to perform select query
     */
    @Override
    public BlobItem getItem(long id, int idVolume) throws GenericBlobStorageException, SQLException {
        return configProvider.getBlobVolumes()
                .values()
                .stream()
                .filter(blobVolume -> blobVolume.getVolumeId() == idVolume)
                .findFirst()
                .orElseThrow(() -> {
                    throw new NoSuchBlobVolumeException(MessageFormat.format("There is no such blob volume with ID {0}", idVolume));
                })
                .getById(id);
    }

    /**
     * Deletes a blob item with a given ID and volume ID. Returns nothing normally.
     *
     * @param id       ID of entity
     * @param idVolume ID of volume
     * @throws SQLException                it attempts to perform delete SQL-query
     * @throws GenericBlobStorageException it is possible that there will be no such blob volume
     */
    @Override
    public void deleteItem(long id, int idVolume) throws SQLException, GenericBlobStorageException {
        configProvider.getBlobVolumes()
                .values()
                .stream()
                .filter(blobVolume -> blobVolume.getVolumeId() == idVolume)
                .findFirst()
                .orElseThrow(() -> {
                    throw new NoSuchBlobVolumeException(MessageFormat.format("There is no such blob volume with ID {0}", idVolume));
                })
                .delete(id);
    }

    /**
     * Creates a new blob volume calculating its new ID according
     * to the existing blob volumes.
     *
     * @return new blob volume
     * @throws SQLException it will try to execute DDL query
     */
    @Override
    public synchronized BlobVolume createNewBlobVolume() throws SQLException {
        int blobVolumeIndex = this.configProvider.getBlobVolumes().size() + 1;
        BlobVolume blobVolume = this.configProvider.getBlobVolumeProvider().getObject();
        blobVolume.initialize(blobVolumeIndex);
        this.configProvider.appendToDatabaseVolumes(blobVolume.getVolumeFile());
        this.configProvider.getBlobVolumes().put(blobVolume.getVolumeName(), blobVolume);
        return blobVolume;
    }

    /**
     * Returns suitable database volume that has free space to store that
     * should be normally lesser than maximal blob volume size in bytes.
     *
     * @param desiredSpace space of entity we want to store
     * @return a file of suitable database to store the entity
     */
    @Override
    public BlobVolume getSuitableBlobVolume(long desiredSpace) throws GenericBlobStorageException {
        // If there is no volumes at all then return null immediately
        if (this.configProvider.getDatabaseVolumes().size() == 0) {
            return null;
        }

        // Scroll all the existing volumes and returns the one that has enough space to
        // store the entity. Return null if no such volumes are presented.
        File suitableDatabaseVolume = null;
        synchronized (this) {
            for (File databaseVolume : this.configProvider.getDatabaseVolumes()) {
                if (databaseVolume.length() + desiredSpace <= this.configProvider.getMaxBlobVolumeSizeInBytes()) {
                    suitableDatabaseVolume = databaseVolume;
                    break;
                }
            }
        }

        // We want to return null in case if no suitable File object is found
        // else we will extract corresponding BlobVolume object.
        return suitableDatabaseVolume == null
                ? null
                : this.configProvider.getBlobVolumes().get(suitableDatabaseVolume.getName());
    }

    /**
     * Checks mime-type that it's not null, not empty and in allowed list of
     * mime-types.
     *
     * @param mimeType mime-type to check
     * @throws GenericBlobStorageException it will crush if mime-type is not ok
     */
    private void checkMimeType(String mimeType) throws GenericBlobStorageException {
        if (mimeType == null) {
            throw new IncorrectMimeTypeException("Mime type should be defined to store the item");
        }
        if (mimeType.equals("")) {
            throw new IncorrectMimeTypeException("Empty mime type is not allowed to store the item");
        }
        if (mimeType.length() < 2 || mimeType.length() > 255) {
            throw new IncorrectMimeTypeException(
                    MessageFormat.format(
                            "Mime type {0} is not allowed for the item, length should be between 2 and 255 characters",
                            mimeType
                    )
            );
        }
        if (!this.configProvider.getAllowedMimeTypes().contains(mimeType)) {
            throw new IncorrectMimeTypeException(MessageFormat.format("This mime type is not allowed: {0}", mimeType));
        }
    }

    /**
     * Checks a given size to be corresponding to max allowed size to store
     * blob item.
     *
     * @param size size of item to store in bytes
     * @throws GenericBlobStorageException it will crush if size is not ok
     */
    private void checkBlobItemSize(long size) throws GenericBlobStorageException {
        long maxAllowedSize = this.configProvider.getMaxBlobItemSize();
        if (size > maxAllowedSize) {
            throw new TooBigItemException(MessageFormat.format("Size to store is too big {0}, max allowed size is {1} bytes", size, maxAllowedSize));
        }
    }
}
