package org.jstart.carrot.config;

public enum EResultCode {
    /**
     * 操作成功=200
     */
    SUCCESS(200, "Operation successful"),
    /**
     * 部分成功=210
     */
    PARTIAL_SUCCESS(210, "The operation part was successful"),
    /**
     * 操作失败=300
     */
    FAILED(300,"Operation failed"),
    /**
     * 参数校验失败=301
     */
    VALIDATE_FAILED(301,"Parameter verification failed"),
    /**
     * 跳转
     */
    REDIRECT(302,"Jump to"),
    /**
     * 未登录或token已过期=401
     */
    UNAUTHORIZED(401,"Not logged in or login credentials have expired"),
    /**
     * 禁止访问=402
     */
    FORBIDDEN_ACCESS(402,"Prohibit access"),
    /**
     * 权限不足=403
     */
    FORBIDDEN(403,"Insufficient permissions"),
    /**
     * 资源不存在=404
     */
    NOT_FOUND(404,"Resources do not exist"),
    /**
     * 签名验证失败=407
     */
    SIGN_NOT_PASS(407,"Signature verification failed"),
    /**
     * 资源被锁定，没有取到锁，稍后重试=423
     */
    LOCK(423,"The resources are locked. Please try again later"),
    /**
     * 系统错误繁忙
     */
    ERROR(500,"The system is busy or contact the administrator")
    ;

    private final   int code;
    private  final String message;

    EResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public Integer getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    public static EResultCode getByCode(int code) {
        for (EResultCode value : values()) {
            if (value.code==(code)) {
                return value;
            }
        }
        return null;
    }
}
