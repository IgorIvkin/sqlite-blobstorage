package com.igorivkin.blobstorage.blobitem;

import com.igorivkin.blobstorage.exceptions.GenericBlobStorageException;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Set;

@Component
public class BlobItemValidator {
    private static final Set<Integer> allowedStatuses = Set.of(
            BlobItemStatus.PENDING,
            BlobItemStatus.COMMITTED,
            /*
             * This status is not used for now. All the entities are naturally
             * deleting from database. But we can change this logic and start
             * to mark them as deleted instead.
             */
            BlobItemStatus.DELETED
    );

    /**
     * Validates a given item to be corresponding to our restrictions.
     * @param item item to validate
     * @return true if item is ok
     * @throws GenericBlobStorageException the validator will crush in case item is bad
     */
    public boolean validate(BlobItem item) throws GenericBlobStorageException {
        if(!BlobItemValidator.allowedStatuses.contains(item.getStatus())) {
            throw new GenericBlobStorageException(MessageFormat.format("Status {0} is not allowed for the item", item.getStatus()));
        }
        return true;
    }
}
