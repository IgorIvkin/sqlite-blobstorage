package com.igorivkin.blobstorage.blobitem;

import com.igorivkin.blobstorage.exceptions.GenericDatabaseException;
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
     * @throws GenericDatabaseException the validator will crush in case item is bad
     */
    public boolean validate(BlobItem item) throws GenericDatabaseException {
        if(!BlobItemValidator.allowedStatuses.contains(item.getStatus())) {
            throw new GenericDatabaseException(MessageFormat.format("Status {0} is not allowed for the item", item.getStatus()));
        }

        String mimeType = item.getMimeType();
        if(mimeType.length() < 2 || mimeType.length() > 255) {
            throw new GenericDatabaseException(MessageFormat.format("Mime type {0} is not allowed for the item, length should be between 2 and 255 characters",
                                                                    mimeType)
            );
        }
        return true;
    }
}
