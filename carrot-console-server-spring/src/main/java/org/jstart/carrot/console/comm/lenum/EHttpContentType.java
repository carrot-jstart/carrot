package org.jstart.carrot.console.comm.lenum;

import java.util.Arrays;

/**
 * http请求类型 枚举
 * @author
 * @date   2019-01-08
 */
public enum EHttpContentType {
    /**
     * application/json
     */
    JSON("application/json", "application/json"),
    /**
     * application/x-www-form-urlencoded
     */
    FORM("application/x-www-form-urlencoded", "application/x-www-form-urlencoded"),
    /**
     * multipart/form-data
     */
    FORMDATA("multipart/form-data", "multipart/form-data"),
    /**
     * application/xml
     */
    XML("application/xml", "application/xml");

    private String desc;//枚举描述
    private String value;//枚举值

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 构造方法
     *
     * @param desc  枚举描述
     * @param value 枚举值
     */
    EHttpContentType(String desc, String value) {
        this.desc = desc;
        this.value = value;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 枚举值
     * @return
     */
    public static EHttpContentType getByValue(String value) {
        return Arrays.stream(EHttpContentType.values())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "EHttpContentType{" +
                "desc='" + desc + '\'' +
                ", value=" + value +
                '}';
    }
}

