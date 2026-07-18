package org.jstart.carrot.console.comm;

public enum EResultCode {
    /**
     * 操作成功=200
     */
    SUCCESS(200, "result.code.SUCCESS", "操作成功"),
    /**
     * 部分成功=210
     */
    PARTIAL_SUCCESS(210, "result.code.PARTIAL_SUCCESS", "操作部分成功"),
    /**
     * 操作失败=300
     */
    FAILED(300, "result.code.FAILED", "操作失败"),
    /**
     * 参数校验失败=301
     */
    VALIDATE_FAILED(301, "result.code.VALIDATE_FAILED", "参数校验失败"),
    /**
     * 跳转
     */
    REDIRECT(302, "result.code.REDIRECT", "跳转"),
    /**
     * 未登录或token已过期=401
     */
    UNAUTHORIZED(401, "result.code.UNAUTHORIZED", "未登录或登录凭证已过期"),
    /**
     * 禁止访问=402
     */
    FORBIDDEN_ACCESS(402, "result.code.FORBIDDEN_ACCESS", "禁止访问"),
    /**
     * 权限不足=403
     */
    FORBIDDEN(403, "result.code.FORBIDDEN", "权限不足"),
    /**
     * 资源不存在=404
     */
    NOT_FOUND(404, "result.code.NOT_FOUND", "资源不存在"),
    /**
     * 签名验证失败=407
     */
    SIGN_NOT_PASS(407, "result.code.SIGN_NOT_PASS", "签名验证失败"),
    /**
     * 资源被锁定，没有取到锁，稍后重试=423
     */
    LOCK(423, "result.code.LOCK", "资源被锁定，稍后重试"),
    /**
     * 系统错误繁忙
     */
    ERROR(500, "result.code.ERROR", "系统繁忙或联系管理员");

    private final int code;
    private final String messageKey;
    private final String message;

    EResultCode(Integer code, String messageKey, String message) {
        this.code = code;
        this.messageKey = messageKey;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getMessage() {
        return message;
    }
}
