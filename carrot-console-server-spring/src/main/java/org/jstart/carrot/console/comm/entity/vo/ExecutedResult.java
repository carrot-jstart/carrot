package org.jstart.carrot.console.comm.entity.vo;

import org.jstart.carrot.console.comm.EResultCode;

import java.io.Serializable;
import java.util.Objects;

/**
 * 返回结果实体
 *
 * @param <T>
 */
public class ExecutedResult<T> implements Serializable {
    private static final String STR_SUCCESS = "success.";
    private static final String STR_FAIL = "failed.";
    /**
     * 请求响应状态码 EResultCode
     */
    private Integer code = EResultCode.FAILED.getCode();
    /**
     * 错误消息
     */
    private String msg = STR_FAIL;
    /**
     * 数据包
     */
    private T data;
    /**
     * 错误码
     */
    private String msgCode = "";

    public ExecutedResult() {
    }

    public ExecutedResult(EResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.data = data;
        this.msgCode = resultCode.getMessageKey();
    }

    public ExecutedResult(EResultCode resultCode, T data, String msg) {
        this.code = resultCode.getCode();
        this.data = data;
        this.msg = msg;
    }

    public ExecutedResult(EResultCode resultCode, T data, String msg, String msgCode) {
        this.code = resultCode.getCode();
        this.data = data;
        this.msg = msg;
        this.msgCode = msgCode;
    }

    public ExecutedResult(T refToken) {
    }

    public Boolean isSuccess() {
        return Objects.equals(this.code, EResultCode.SUCCESS.getCode());
    }

    public Boolean isFailed() {
        return !this.isSuccess();
    }

    public static <T> ExecutedResult<T> success() {
        return new ExecutedResult<>(EResultCode.SUCCESS, null, EResultCode.SUCCESS.getMessage(),
                EResultCode.SUCCESS.getMessageKey());
    }

    public static <T> ExecutedResult<T> success(T data) {
        return new ExecutedResult<>(EResultCode.SUCCESS, data, EResultCode.SUCCESS.getMessage(),
                EResultCode.SUCCESS.getMessageKey());
    }

    public static <T> ExecutedResult<T> success(T data, String msg) {
        if (null == msg || msg.isBlank()) {
            msg = EResultCode.SUCCESS.getMessage();
            return new ExecutedResult<>(EResultCode.SUCCESS, data, msg, EResultCode.SUCCESS.getMessageKey());
        }
        return new ExecutedResult<>(EResultCode.SUCCESS, data, msg);
    }

    public static <T> ExecutedResult<T> success(T data, String msg, EResultCode code) {
        if (null == msg || msg.isBlank()) {
            msg = code.getMessage();
            return new ExecutedResult<>(code, data, msg, code.getMessageKey());
        }
        return new ExecutedResult<>(code, data, msg);
    }

    public static <T> ExecutedResult<T> failed() {
        return ExecutedResult.failed("");
    }

    public static <T> ExecutedResult<T> failed(String msg) {
        return ExecutedResult.failed(msg, EResultCode.ERROR);
    }

    public static <T> ExecutedResult<T> failed(String msg, EResultCode code) {
        return ExecutedResult.failed(null, msg, code);
    }

    public static <T> ExecutedResult<T> failed(T data, String msg, EResultCode code) {
        if (null == msg || msg.isBlank()) {
            msg = code.getMessage();
            return new ExecutedResult<>(code, data, msg, code.getMessageKey());
        }
        return new ExecutedResult<>(code, data, msg);
    }

    /**
     * 参数校验失败返回结果
     */
    public static <T> ExecutedResult<T> validateFailed(String message) {
        return new ExecutedResult<>(EResultCode.VALIDATE_FAILED, null, message);
    }

    /**
     * 未登录返回结果
     */
    public static <T> ExecutedResult<T> unauthorized(T data) {
        return new ExecutedResult<>(EResultCode.UNAUTHORIZED, data, EResultCode.UNAUTHORIZED.getMessage(),
                EResultCode.UNAUTHORIZED.getMessageKey());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        ExecutedResult<?> that = (ExecutedResult<?>) o;
        return Objects.equals(code, that.code) && Objects.equals(msg, that.msg) && Objects.equals(data, that.data)
                && Objects.equals(msgCode, that.msgCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, msg, data, msgCode);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }

    @Override
    public String toString() {
        return "ExecutedResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", msgCode='" + msgCode + '\'' +
                '}';
    }
}
