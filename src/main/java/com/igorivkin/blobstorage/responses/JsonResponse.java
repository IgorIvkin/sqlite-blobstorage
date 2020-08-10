package com.igorivkin.blobstorage.responses;

import java.util.Objects;

/**
 * Basic class representing JSON-response returning by controller.
 */
public class JsonResponse {
    protected String status;
    protected Object result;

    public JsonResponse() {}

    public JsonResponse(String status, Object result) {
        this.status = status;
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonResponse that = (JsonResponse) o;
        return Objects.equals(getStatus(), that.getStatus()) &&
                Objects.equals(getResult(), that.getResult());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getResult());
    }
}
