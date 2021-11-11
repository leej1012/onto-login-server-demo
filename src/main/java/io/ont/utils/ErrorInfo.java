package io.ont.utils;

public enum ErrorInfo {

    /**
     * success
     */
    SUCCESS(0, "SUCCESS"),

    /**
     * not found
     */
    NOT_FOUND(61001, "FAIL, not found."),

    /**
     * verify failed
     */
    VERIFY_FAILED(61002, "FAIL, verify fail."),

    /**
     * kyc owner not match
     */
    KYC_OWNER_NOT_MATCH(61003, "KYC does not match the ONT ID."),

    /**
     * kyc expire
     */
    KYC_EXPIRE(61004, "KYC has expired."),
    ;

    private int errorCode;
    private String errorDescEN;

    ErrorInfo(int errorCode, String errorDescEN) {
        this.errorCode = errorCode;
        this.errorDescEN = errorDescEN;
    }

    public int code() {
        return errorCode;
    }

    public String descEN() {
        return errorDescEN;
    }


}
