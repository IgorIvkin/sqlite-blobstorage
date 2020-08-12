package com.igorivkin.blobstorage.blobstorage;

import com.igorivkin.blobstorage.blobitem.BlobItem;
import com.igorivkin.blobstorage.blobitem.BlobStoredItemAddress;
import com.igorivkin.blobstorage.blobvolume.BlobVolume;
import com.igorivkin.blobstorage.exceptions.GenericBlobStorageException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

@Service
public interface BlobStorage {

    BlobStoredItemAddress storeItem(InputStream itemBinaryStream, String mimeType)
            throws IOException, SQLException, GenericBlobStorageException;

    BlobItem getItem(long id, int idVolume)
            throws SQLException, GenericBlobStorageException;

    BlobVolume createNewBlobVolume() throws SQLException;

    BlobVolume getSuitableBlobVolume(long desiredSpace) throws GenericBlobStorageException;

}
