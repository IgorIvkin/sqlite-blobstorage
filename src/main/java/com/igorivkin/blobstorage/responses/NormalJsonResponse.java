package com.igorivkin.blobstorage.responses;

/**
 * Represents normal JSON-response, mostly for REST-service.
 * This response contains the object of free structure representing
 * the result of operation: status, object, array of objects etc.
 */
public class NormalJsonResponse extends JsonResponse {

    public NormalJsonResponse() {
        super();
        this.status = "ok";
    }

    public NormalJsonResponse(Object result) {
        super("ok", result);
    }

    public NormalJsonResponse(String status, Object result) {
        super(status, result);
    }

}
