package com.igorivkin.blobstorage.responses;

import java.util.Objects;

/**
 * Represents error response, mostly for REST-services.
 * Commonly such response should provide some error code and string
 * message helping to find a workaround.
 */
public class ErrorJsonResponse extends JsonResponse {

    protected String reason;
    protected long errorCode;

    public ErrorJsonResponse() {
        super();
        this.status = "error";
    }

    public ErrorJsonResponse(Object result) {
        super("error", result);
    }

    public ErrorJsonResponse(String status, Object result) {
        super(status, result);
    }

    public ErrorJsonResponse setErrorCode(long errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public long getErrorCode() {
        return errorCode;
    }

    public ErrorJsonResponse setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ErrorJsonResponse that = (ErrorJsonResponse) o;
        return getErrorCode() == that.getErrorCode() &&
                Objects.equals(getReason(), that.getReason());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getReason(), getErrorCode());
    }
}
