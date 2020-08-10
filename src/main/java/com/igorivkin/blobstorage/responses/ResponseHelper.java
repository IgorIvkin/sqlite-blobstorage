package com.igorivkin.blobstorage.responses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This utility class provides shortcuts for some widely
 * required responses. It wraps the returning object to Spring's
 * ResponseEntity<T> and reduces the boilerplate stuff.
 */
public class ResponseHelper {
    public static ResponseEntity<JsonResponse> normalJsonResponse(Object object) {
        return new ResponseEntity<>(
                new NormalJsonResponse(
                        object
                ),
                HttpStatus.OK
        );
    }

    /**
     * This version is intended to return back a string definition of error.
     *
     * @param reason string representation of error
     * @return a response object providing detailed information about error
     */
    public static ResponseEntity<JsonResponse> errorJsonResponse(String reason) {
        return new ResponseEntity<>(
                new ErrorJsonResponse(
                        null
                ).setReason(reason),
                HttpStatus.OK
        );
    }

    /**
     * This version is intended to return back a string definition of error
     * and digital code of error.
     *
     * @param reason string representation of error
     * @param errorCode digital code of error
     * @return a response object providing detailed information about error
     */
    public static ResponseEntity<JsonResponse> errorJsonResponse(String reason, long errorCode) {
        return new ResponseEntity<>(
                new ErrorJsonResponse(
                        null
                )
                        .setReason(reason)
                        .setErrorCode(errorCode),
                HttpStatus.OK
        );
    }

    /**
     * This version is intended to return back some meaningful information and
     * not only the reason and the error code.
     *
     * @param object data to return back with error response
     * @param reason string representation of error
     * @param errorCode digital code of error
     * @return a response object providing detailed information about error
     */
    public static ResponseEntity<JsonResponse> errorJsonResponse(Object object, String reason, long errorCode) {
        return new ResponseEntity<>(
                new ErrorJsonResponse(
                        object
                )
                        .setReason(reason)
                        .setErrorCode(errorCode),
                HttpStatus.OK
        );
    }
}
