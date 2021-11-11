package io.ont.exception;


public class OntoLoginException extends RuntimeException {

    private String errDesEN;

    private int errCode;

    private String action;

    public OntoLoginException(String msg) {
        super(msg);
    }

    public OntoLoginException() {
        super();
    }

    public OntoLoginException(String action, String errDesEN, int errCode) {
        this.action = action;
        this.errDesEN = errDesEN;
        this.errCode = errCode;
    }

    public String getErrDesEN() {
        return errDesEN;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getAction() {
        return action;
    }
}
