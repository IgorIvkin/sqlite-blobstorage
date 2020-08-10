package com.igorivkin.blobstorage.controllers.api;

import com.igorivkin.blobstorage.blobitem.BlobItem;
import com.igorivkin.blobstorage.blobitem.BlobStoredItemAddress;
import com.igorivkin.blobstorage.blobstorage.BlobStorage;
import com.igorivkin.blobstorage.exceptions.*;
import com.igorivkin.blobstorage.responses.JsonResponse;
import com.igorivkin.blobstorage.responses.ResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;

@RestController
@RequestMapping(value = "/api/")
public class StorageApiController {

    /**
     * This blob storage actually behaves like a service here.
     */
    private final BlobStorage blobStorage;

    @Autowired
    public StorageApiController(BlobStorage blobStorage) {
        this.blobStorage = blobStorage;
    }

    @PostMapping(value = "/store_file/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonResponse> processStoreFile(@RequestParam(name = "file_to_store") MultipartFile fileToStore) {
        try {
            BlobStoredItemAddress itemAddress = this.blobStorage.storeItem(fileToStore.getInputStream(), fileToStore.getContentType());
            return ResponseHelper.normalJsonResponse(
                    itemAddress
            );
        } catch(TooBigItemException exception) {
            return ResponseHelper.errorJsonResponse(
                    MessageFormat.format("The item is too big to store. Reason: {0}", exception.getMessage())
            );
        }
        catch(IncorrectMimeTypeException exception) {
            return ResponseHelper.errorJsonResponse(
                    MessageFormat.format("The item has incorrect mime-type. Reason: {0}", exception.getMessage())
            );
        }
        catch(IOException exception) {
            return ResponseHelper.errorJsonResponse(
                    MessageFormat.format("Unknown I/O error. Reason: {0}", exception.getMessage())
            );
        }
        catch(GenericBlobStorageException | GenericDatabaseException | SQLException exception) {
            return ResponseHelper.errorJsonResponse(
                    MessageFormat.format("An error was occurred while saving the file. Reason: {0}", exception.getMessage())
            );
        }
    }

    @GetMapping(value = "get_file", produces = {
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            MediaType.TEXT_PLAIN_VALUE
    })
    public ResponseEntity<?> processGetFile(@RequestParam(name = "id") long id,
                                            @RequestParam(name = "volume_id") int volumeId) throws GenericBlobStorageException, SQLException {
        // TODO: catch possible errors and exceptions here
        try {
            BlobItem item = this.blobStorage.getItem(id, volumeId);
            if(item != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.valueOf(item.getMimeType()));
                headers.setCacheControl(CacheControl.noCache().getHeaderValue());
                return new ResponseEntity<>(item.getContent(), headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
            }
        } catch (NoSuchBlobVolumeException exception) {
            // In case if "no such volume" exception occurred then it means we passed
            // wrong volume id in parameters. So just return 404.
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        }
    }
}
