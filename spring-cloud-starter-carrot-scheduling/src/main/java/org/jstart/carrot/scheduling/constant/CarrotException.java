package org.jstart.carrot.scheduling.constant;


public class CarrotException extends RuntimeException {

    private EResultCode code;

    public CarrotException(){super();}

    public CarrotException(String message){
        super(message);
        this.code = EResultCode.ERROR;
    }
    public CarrotException(EResultCode code, String msg){
        super(msg);
        this.code = code;
    }
    public CarrotException(EResultCode eResultCode){
        super(eResultCode.getMessage());
        this.code = eResultCode;
    }

    public CarrotException(String msg, Throwable cause){
        super(msg,cause);
    }

    public CarrotException(Throwable cause){ super(cause); }

    public EResultCode getCode() {
        return code;
    }

    public void setCode(EResultCode code) {
        this.code = code;
    }
}
