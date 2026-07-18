package org.jstart.carrot.console.comm.validator;

public class ParameterValidateResult {
    private Boolean isSuccess;
    private String errorMsg;

    public ParameterValidateResult() {}
    public ParameterValidateResult(Boolean isSuccess, String errorMsg) {
        this.isSuccess = isSuccess;
        this.errorMsg = errorMsg;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }
    public void setIsSuccess(Boolean success) {
        isSuccess = success;
    }

    public Boolean getIsFiled() {
        return !isSuccess;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "ParameterValidateResult{" +
                "isSuccess=" + isSuccess +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}