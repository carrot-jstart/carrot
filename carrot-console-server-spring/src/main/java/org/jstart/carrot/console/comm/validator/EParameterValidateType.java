package org.jstart.carrot.console.comm.validator;

import java.util.Arrays;


public enum EParameterValidateType {
    /**
     * 不能为空
     */
    NOT_NULLOREMPTY(", the value can't be null or empty", 1),



    /**
     * 必须大于
     */
    MUST_GREATER(", the value must be greater than ", 2),
    /**
     * 必须大于等于
     */
    MUST_GREATER_THAN(", the value must be greater than or equal ", 3),



    /**
     * 必须小于
     */
    MUST_LESS(", the value must be less than ", 4),
    /**
     * 必须小于等于
     */
    MUST_LESS_THAN(", the value must be less than or equal ", 5),



    /**
     * 必须是枚举
     */
    MUST_ENUM(", the value must be a enum value of ", 6),



    /**
     * 必须是有效的日期（yyyy-MM-dd）
     */
    MUST_DATE(", the value must be a valid “date”（yyyy-MM-dd）", 7),
    /**
     * 必须是有效的时间（yyyy-MM-dd HH:mm:ss）
     */
    MUST_DATE_TIME(", the value must be a valid “datetime”（yyyy-MM-dd HH:mm:ss）", 8),



    /**
     * 必须是有效电话号码
     */
    MUST_PHONE_NUMBER(", the value must be a valid phone number", 9),
    /**
     * 必须是有效座机号码
     */
    MUST_TELEPHONE_NUMBER(", the value must be a valid telphone number", 10),
    /**
     * 必须是有效手机号码
     */
    MUST_MOBILE_NUMBER(", the value must be a valid mobile phone number", 11),



    /**
     * 必须是有效电子邮箱地址
     */
    MUST_EMAIL(", the value must be a valid email address", 12),

    /**
     * 不能超出取值范围
     */
    MUST_IN_RANGE(", the value can't out of the range of ", 13),

    /**
     * 长度不能小于
     */
    MUST_LENGTH_MIN(", the length is not less than ", 14),
    /**
     * 长度不能大于
     */
    MUST_LENGTH_MAX(", the length is not greater than ", 15),

    /**
     * 判断是否相等
     */
    MUST_EQUALS(", the length is not greater than ", 16)
    ;

    private String desc;//枚举描述
    private int value;//枚举值

    public int getValue() {
        return value;
    }
    public String getDesc() {
        return desc;
    }

    /**
     * 构造方法
     * @param desc 枚举描述
     * @param value 枚举值
     */
    private EParameterValidateType(String desc, int value) {
        this.desc = desc;
        this.value = value;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 枚举值
     * @return
     */
    public static EParameterValidateType getByValue(int value) {
        return Arrays.stream(EParameterValidateType.values())
                .filter(e -> e.getValue() == value)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "EParameterValidateType{" +
                "desc='" + desc + '\'' +
                ", value=" + value +
                '}';
    }
}