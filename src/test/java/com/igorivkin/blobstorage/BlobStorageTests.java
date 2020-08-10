package com.igorivkin.blobstorage;

import com.igorivkin.blobstorage.blobitem.BlobItem;
import com.igorivkin.blobstorage.blobstorage.BlobStorage;
import com.igorivkin.blobstorage.blobitem.BlobStoredItemAddress;
import com.igorivkin.blobstorage.blobvolume.BlobVolume;
import com.igorivkin.blobstorage.exceptions.GenericBlobStorageException;
import com.igorivkin.blobstorage.exceptions.GenericDatabaseException;
import com.igorivkin.blobstorage.exceptions.TooBigItemException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

// TODO: prepare dedicated config file for testing
// at the moment it uses default config file and if the default values will be changed then tests could be failed

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BlobStorageTests {
    @Autowired
    BlobStorage blobStorage;

    @BeforeAll
    public void setup() throws SQLException {
        blobStorage.createNewBlobVolume();
    }

    @Test
    public void checkSuitableBlobVolumesSuccess() throws GenericBlobStorageException {
        // To be working this test is required for configuration file that defines
        // "maxBlobVolumeSize" more than 345 Kb.
        BlobVolume suitableVolume = blobStorage.getSuitableBlobVolume(345_000L);
        assertNotNull(suitableVolume);
    }

    @Test
    public void checkSuitableBlobVolumesWithTooMuchDesiredSpace() throws GenericBlobStorageException {
        // To be working this test is required for configuration file that defines
        // "maxBlobVolumeSize" lesser than 50 Mb.
        BlobVolume suitableVolume = blobStorage.getSuitableBlobVolume(50_000_000L);
        assertNull(suitableVolume);
    }

    @Test
    public void checkNotAllowedMimeType() {
        Exception exception = assertThrows(GenericBlobStorageException.class, () -> {
            // Absolutely doesn't matter that data binary stream here is null,
            // the check for mime-type becomes before of the analyzing the content
            // so the code is expected to crush correctly.
            blobStorage.storeItem(null, "bad/mimetype");
        });
        assertTrue(exception.getMessage().startsWith("This mime type is not allowed"));
    }

    @Test
    public void checkStoreValidItem() throws SQLException, GenericBlobStorageException, GenericDatabaseException, IOException {
        String content = "Text content";
        InputStream targetStream = new ByteArrayInputStream(content.getBytes());
        BlobStoredItemAddress storedItemAddress = blobStorage.storeItem(targetStream, "text/plain");
        BlobItem databaseItem = blobStorage.getItem(storedItemAddress.getId(), storedItemAddress.getVolumeId());
        assertArrayEquals(databaseItem.getContent(), "Text content".getBytes());
    }

    @Test
    public void checkStoreTooBigItem() throws SQLException, GenericBlobStorageException, GenericDatabaseException, IOException {
        byte[] tooBigItem = new byte[30_000_000];
        InputStream targetStream = new ByteArrayInputStream(tooBigItem);
        Exception exception = assertThrows(TooBigItemException.class, () -> {
            BlobStoredItemAddress storedItemAddress = blobStorage.storeItem(targetStream, "text/plain");
        });
        assertTrue(exception.getMessage().startsWith("Size to store is too big"));
    }
}
