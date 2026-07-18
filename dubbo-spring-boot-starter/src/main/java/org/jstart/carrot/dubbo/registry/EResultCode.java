package org.jstart.carrot.dubbo.registry;

public enum EResultCode {
    /**
     * 操作成功=200
     */
    SUCCESS(200, "操作成功"),
    /**
     * 部分成功=210
     */
    PARTIAL_SUCCESS(210, "操作部分成功"),
    /**
     * 操作失败=300
     */
    FAILED(300,"操作失败"),
    /**
     * 参数校验失败=301
     */
    VALIDATE_FAILED(301,"参数校验失败"),
    /**
     * 跳转
     */
    REDIRECT(302,"跳转"),
    /**
     * 未登录或token已过期=401
     */
    UNAUTHORIZED(401,"未登录或登录凭证已过期"),
    /**
     * 禁止访问=402
     */
    FORBIDDEN_ACCESS(402,"禁止访问"),
    /**
     * 权限不足=403
     */
    FORBIDDEN(403,"权限不足"),
    /**
     * 资源不存在=404
     */
    NOT_FOUND(404,"资源不存在"),
    /**
     * 签名验证失败=407
     */
    SIGN_NOT_PASS(407,"签名验证失败"),
    /**
     * 资源被锁定，没有取到锁，稍后重试=423
     */
    LOCK(423,"资源被锁定，稍后重试"),
    /**
     * 系统错误繁忙
     */
    ERROR(500,"系统繁忙或联系管理员")
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
}
