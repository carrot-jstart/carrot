package org.jstart.carrot.console.comm.utils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 数值处理类
 * @author carrot
 * @date   2024-5-12
 */
public final class NumericUtil {
    private NumericUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 验证数值为空
     * @param num 传入数值
     */
    public static Boolean isNullOrEmpty(Number num) {
        return num==null || num.doubleValue()==0;
    }

    /**
     * 验证数值非空
     * @param num 传入数值
     */
    public static Boolean isNotNullOrEmpty(Number num) {
        return !NumericUtil.isNullOrEmpty(num);
    }

    /**
     * 判断传入值，是否在传入数组中
     * @param num 比较值
     * @param array 范围数组
     */
    public static Boolean includeIn(Number num, Number... array) {
        return ListUtil.toList(array).contains(num);
    }

    /**
     * 字符串枚举值转成枚举对象
     * @author linliu
     * @date   2018-12-24
     * @param enumType 枚举类型
     * @param value 枚举值
     * @param <T> 枚举类型
     * @return 枚举对象
     */
    public static <T extends Enum<T>> T parseEnum(Class<T> enumType, Integer value) {
        if(null == value) {
            return null;
        }

        T result = null;
        try {
            T[] values = enumType.getEnumConstants();
            Method getValue = enumType.getMethod("getValue");

            for (T e : values) {
                if(getValue.invoke(e).equals(value)) {
                    result = e;
                    break;
                }
            }
        } catch (Exception e) {
            //TODO log
        }
        return result;
    }

    /**
     * 判断枚举是否申明了传入枚举值
     * @author linliu
     * @date   2018-12-24
     * @param enumType 枚举类型
     * @param value 枚举值
     * @param <T> 枚举类型
     * @return 是否包含
     */
    public static <T extends Enum<T>> boolean isEnum(Class<T> enumType, Integer value) {
        return (null != NumericUtil.parseEnum(enumType, value));
    }

    /**
     * 尝试转换为 Integer
     * @param value 传入值
     */
    public static Integer tryParseInt(Object value) {
        return NumericUtil.tryParseInt(value, Integer.MIN_VALUE);
    }

    /**
     * 尝试转换为 Long
     * @param value 传入值
     */
    public static Long tryParseLong(Object value) {
        return NumericUtil.tryParseLong(value, Long.MIN_VALUE);
    }

    /**
     * 尝试转换为 Double
     * @param value 传入值
     */
    public static Double tryParseDouble(Object value) {
        return NumericUtil.tryParseDouble(value, Double.valueOf(String.valueOf(Long.MIN_VALUE)));
    }

    /**
     * 尝试转换为 BigDecimal
     * @param value 传入值
     */
    public static BigDecimal tryParseBigDecimal(Object value) {
        return NumericUtil.tryParseBigDecimal(value, new BigDecimal(String.valueOf(Long.MIN_VALUE)));
    }

    /**
     * 输出数值补“0”，不输出小数部分（如果传入数值位数不足传入最小位数，则在数值前补“0”，否则直接输出数值的字符串）
     * @param value 传入数值
     * @param minLength 最小位数
     */
    public static String toDString(Number value, Integer minLength) {
        String result = toNumberString(value, 0);
        StringBuilder sb = new StringBuilder();
        while (result.length()+sb.length() < minLength) {
            sb.append("0");
        }
        sb.append(result);
        return sb.toString();
    }

    /**
     * 判断传入数值大小是否在“min”和“max”之间（不包含包含端值，即包含最大最小值）
     * @param num 比较的数值
     * @param min 最小值
     * @param max 最大值
     */
    public static Boolean isInRange(Number num, Number min, Number max) {
        BigDecimal dnum = tryParseBigDecimal(num);
        BigDecimal dmin = tryParseBigDecimal(min);
        BigDecimal dmax = tryParseBigDecimal(max);
        return dnum.compareTo(dmin)>0 && dnum.compareTo(dmax)<0;
    }

    /**
     * 判断传入数值大小是否在“min”和“max”之间（包含端值，即包含最大最小值）
     * @param num 比较的数值
     * @param min 最小值
     * @param max 最大值
     */
    public static Boolean isInRangeInclude(Number num, Number min, Number max) {
        BigDecimal dnum = tryParseBigDecimal(num);
        BigDecimal dmin = tryParseBigDecimal(min);
        BigDecimal dmax = tryParseBigDecimal(max);
        return dnum.compareTo(dmin)>-1 && dnum.compareTo(dmax)<1;
    }

    /**
     * 格式化输出小数（最多“length”位小数，如果小数部分某位后面全是0，则不展示）
     * @param num 数字
     * @param length 最多展示的小数位数
     */
    public static String toNumberString(Number num, Integer length) {
        length = tryParseInt(length);
        StringBuilder sbFormat = new StringBuilder("#0");
        if(length.compareTo(0) > 0) {
            sbFormat.append(".");
            for(int i=0; i<length; i++) {
                sbFormat.append("#");
            }
        }
        DecimalFormat df = new DecimalFormat(sbFormat.toString());
        return df.format(num);
    }

    /**
     * 尝试转换为 Integer
     * @param value 传入值
     * @param defaultValue 传入值为空，或者转换失败时，返回的默认值
     */
    public static Integer tryParseInt(Object value, Integer defaultValue) {
        if(null==value) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 尝试转换为 Long
     * @param value 传入值
     * @param defaultValue 传入值为空，或者转换失败时，返回的默认值
     */
    public static Long tryParseLong(Object value, Long defaultValue) {
        if(null==value) {
            return defaultValue;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 尝试转换为 Double
     * @param value 传入值
     * @param defaultValue 传入值为空，或者转换失败时，返回的默认值
     */
    public static Double tryParseDouble(Object value, Double defaultValue) {
        if(null==value) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 尝试转换为 BigDecimal
     * @param value 传入值
     * @param defaultValue 传入值为空，或者转换失败时，返回的默认值
     */
    public static BigDecimal tryParseBigDecimal(Object value, BigDecimal defaultValue) {
        if(null==value) {
            return defaultValue;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}