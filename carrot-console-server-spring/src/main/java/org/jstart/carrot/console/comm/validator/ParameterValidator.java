package org.jstart.carrot.console.comm.validator;




import org.jstart.carrot.console.comm.ConstantFactory;
import org.jstart.carrot.console.comm.utils.ListUtil;
import org.jstart.carrot.console.comm.utils.LocalDateTimeUtil;
import org.jstart.carrot.console.comm.utils.NumericUtil;
import org.jstart.carrot.console.comm.utils.StringUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.function.Function;


public class ParameterValidator {
    /**
     * 验证参数列表
     */
    public List<ParameterValidateItem> listParameter;
    public List<ParameterValidateItem> getListParameter() {
        return listParameter;
    }
    public void setListParameter(List<ParameterValidateItem> listParameter) {
        this.listParameter = listParameter;
    }

    public ParameterValidator() {
        listParameter = new ArrayList<>();
    }

    /**
     * 添加一个需要验证的参数
     * @param item 参数验证对象
     */
    public ParameterValidator addParameter(ParameterValidateItem... item) {
        this.listParameter.addAll(Arrays.asList(item));
        return this;
    }

    /**
     * 添加一个需要验证的参数
     * @param fieldName 参数名称“自行构建参数描述"userName" 或者 "the parameter which named “userName”"”
     * @param type 需要验证的类型
     * @param value 参数的值
     * @param validateFunc 是否通过验证的自函数
     */
    public ParameterValidator addParameter(String fieldName, EParameterValidateType type, Object value, Function<Object, Boolean> validateFunc) {
        this.listParameter.add(new ParameterValidateItem(fieldName, type, value, validateFunc));
        return this;
    }



    /**
     * 添加一个“不能为空”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     */
    public ParameterValidator addNotNullOrEmpty(String fieldName, Object value) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.NOT_NULLOREMPTY, value));
        return this;
    }
    /**
     * 添加一个“必须是一个有效日期”（yyyy-MM-dd）的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     */
    public ParameterValidator addMustDate(String fieldName, Object value) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_DATE, value));
        return this;
    }
    /**
     * 添加一个“必须是一个有效时间”（yyyy-MM-dd HH:mm:ss）的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     */
    public ParameterValidator addMustDateTime(String fieldName, Object value) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_DATE_TIME, value));
        return this;
    }
    /**
     * 添加一个“必须是指定枚举类型的有效值”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     * @param enumType 枚举类型
     */
    public <T extends Enum> ParameterValidator addMustEnum(String fieldName, Object value, Class<T> enumType) {
        this.listParameter.add(new ParameterValidateItem(fieldName, value, enumType));
        return this;
    }
    /**
     * 添加一个“必须是有效电子邮箱地址”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     */
    public ParameterValidator addMustEmail(String fieldName, Object value) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_EMAIL, value));
        return this;
    }
    /**
     * 添加一个“必须是有效手机号码”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     */
    public ParameterValidator addMustMoblie(String fieldName, Object value) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_MOBILE_NUMBER, value));
        return this;
    }



    /**
     * 添加一个“必须大于最小值”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     * @param minValue 校验允许的最小值
     */
    public ParameterValidator addGreater(String fieldName, Object value, Object minValue) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_GREATER, value, minValue, null));
        return this;
    }
    /**
     * 添加一个“必须大于等于最小值”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     * @param minValue 校验允许的最小值
     */
    public ParameterValidator addGreaterThan(String fieldName, Object value, Object minValue) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_GREATER_THAN, value, minValue, null));
        return this;
    }



    /**
     * 添加一个“必须小于最大值”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     * @param maxValue 校验允许的最大值
     */
    public ParameterValidator addLess(String fieldName, Object value, Object maxValue) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_LESS, value, null, maxValue));
        return this;
    }
    /**
     * 添加一个“必须小于等于最大值”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     * @param maxValue 校验允许的最大值
     */
    public ParameterValidator addLessThan(String fieldName, Object value, Object maxValue) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_LESS_THAN, value, null, maxValue));
        return this;
    }



    /**
     * 添加一个“字符串必须大于等于最小长度”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     * @param minLength 校验允许的最小长度
     */
    public ParameterValidator addLengthMin(String fieldName, Object value, Integer minLength) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_LENGTH_MIN, value, minLength, null, null));
        return this;
    }
    /**
     * 添加一个“字符串必须小于等于最大长度”的参数 验证
     * @param fieldName 参数名称
     * @param value 参数值
     * @param maxLength 校验允许的最大长度
     */
    public ParameterValidator addLengthMax(String fieldName, Object value, Integer maxLength) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_LENGTH_MAX, value, null, maxLength, null));
        return this;
    }


    /**
     * 执行验证参数
     * @return 验证结果
     */
    public ParameterValidateResult validate() {
        if(ListUtil.isNotNullOrEmpty((this.listParameter))) {
            return this.checkParameterValidate();
        }
        return new ParameterValidateResult(true, "");
    }

    private ParameterValidateResult checkParameterValidate() {
        for (ParameterValidateItem item : this.listParameter) {
            ParameterValidateResult result = this.checkParameterValidateItem(item);
            if(result.getIsFiled()) {
                return result;
            }
        }
        return new ParameterValidateResult(true, "");
    }
    private ParameterValidateResult checkParameterValidateItem(ParameterValidateItem item) {
        Function<Object, Boolean> func = item.getValidateFunc();
        if(null != func) {
            if(func.apply(item.getValue())) {
                return new ParameterValidateResult(true, "");
            } else {
                return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc());
            }
        }
        switch (item.getType()) {
            case NOT_NULLOREMPTY:
                if(!ParameterValidator.checkNotNull(item.getValue())) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc());
                }
                break;



            case MUST_ENUM:
                if(!ParameterValidator.chekIsEnum(item.getValue(), item.getEnumType())) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc()+item.getEnumType().getName().replace(item.getEnumType().getPackage().getName()+".", "")+"："+item.getValue());
                }
                break;
            case MUST_IN_RANGE:
                if(!ParameterValidator.checkInRange(item.getValue(), item.getRangeMin(), item.getRangeMax())) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc()+item.getEnumType().getTypeName());
                }
                break;
            default:
                return this.checkMustGreaterLess(item);
        }
        return new ParameterValidateResult(true, "");
    }
    private ParameterValidateResult checkMustGreaterLess(ParameterValidateItem item) {
        switch (item.getType()) {
            case MUST_GREATER:
                if(!ParameterValidator.checkGreater(item.getValue(), item.getRangeMin())) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc()+item.getRangeMin().toString());
                }
                break;
            case MUST_GREATER_THAN:
                if(!ParameterValidator.checkGreaterThan(item.getValue(), item.getRangeMin())) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc()+item.getRangeMin().toString());
                }
                break;
            case MUST_LESS:
                if(!ParameterValidator.checkLess(item.getValue(), item.getRangeMax())) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc()+item.getRangeMax().toString());
                }
                break;
            case MUST_LESS_THAN:
                if(!checkLessThan(item.getValue(), item.getRangeMax())) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc()+item.getRangeMax().toString());
                }
                break;
            default:
                return this.checkDateTime(item);
        }
        return new ParameterValidateResult(true, "");
    }
    private ParameterValidateResult checkDateTime(ParameterValidateItem item) {
        switch (item.getType()) {
            case MUST_DATE:
                if(!checkIsMatch(item.getValue(), item.getType()) || !LocalDateTimeUtil.getDate(item.getValue().toString()).isAfter(LocalDateTimeUtil.DEFAULT_DATE)) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc());
                }
                break;
            case MUST_DATE_TIME:
                if(!checkIsMatch(item.getValue(), item.getType()) || !LocalDateTimeUtil.getDateTime(item.getValue().toString()).isAfter(LocalDateTimeUtil.DEFAULT_DATETIME)) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc());
                }
                break;
            default:
                return this.checkLength(item);
        }
        return new ParameterValidateResult(true, "");
    }
    private ParameterValidateResult checkLength(ParameterValidateItem item) {
        switch (item.getType()) {
            case MUST_LENGTH_MIN:
                String strMin = item.getRangeMin().toString();
                int minLength = Integer.valueOf(strMin);
                if(null==item.getValue() || item.getValue().toString().length()<minLength) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc()+strMin);
                }
                break;
            case MUST_LENGTH_MAX:
                String strMax = item.getRangeMax().toString();
                int maxLength = Integer.valueOf(strMax);
                if(null!=item.getValue() && item.getValue().toString().length()>maxLength) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc()+strMax);
                }
                break;
            default:
                return this.checkOther(item);
        }
        return new ParameterValidateResult(true, "");
    }
    private ParameterValidateResult checkOther(ParameterValidateItem item) {
        switch (item.getType()) {
            case MUST_EMAIL:
            case MUST_MOBILE_NUMBER:
            case MUST_TELEPHONE_NUMBER:
            case MUST_PHONE_NUMBER:
                if(!checkIsMatch(item.getValue(), item.getType())) {
                    return new ParameterValidateResult(false, item.getFieldName()+item.getType().getDesc());
                }
                break;
                default:
                    break;
        }
        return new ParameterValidateResult(true, "");
    }

    /**
     * 验证传入参数“不为空”（非null且非空）
     * @param value 参数值
     * @return 是否“不为空”
     */
    public static Boolean checkNotNull(Object value) {
        if(null == value) {
            return false;
        }
        if(value instanceof String) {
            String realValue = (String) value;
            return realValue.replaceAll(ConstantFactory.STR_REGEX_SPACE, "").length()>0;
        } else if(value instanceof List) {
            List<?> realValue = (List)value;
            return ListUtil.isNotNullOrEmpty(realValue);
        }
        return true;
    }

    /**
     * 验证传入参数是否“大于”传入最小值
     * @param value 参数值
     * @param minValue 校验允许的最小值
     * @return 是否“大于”最小值
     */
    public static Boolean checkGreater(Object value, Object minValue) {
        if(null == value) {
            return false;
        }
        if(value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof BigDecimal) {
            BigDecimal realValue = new BigDecimal(value.toString());
            BigDecimal realMin = new BigDecimal(minValue.toString());
            return realValue.compareTo(realMin)>0;
        } else if(value instanceof Calendar) {
            Calendar realValue = (Calendar)value;
            Calendar realMin = (Calendar)minValue;
            return realValue.compareTo(realMin)>0;
        } else if(value instanceof LocalDateTime) {
            LocalDateTime realValue = (LocalDateTime)value;
            LocalDateTime realMin = (LocalDateTime)minValue;
            return realValue.compareTo(realMin)>0;
        } else if(value instanceof LocalDate) {
            LocalDate realValue = (LocalDate)value;
            LocalDate realMin = (LocalDate)minValue;
            return realValue.compareTo(realMin)>0;
        }
        return true;
    }

    /**
     * 验证传入参数是否“大于等于”传入最小值
     * @param value 参数值
     * @param minValue 校验允许的最小值
     * @return 是否“大于等于”传入最小值
     */
    public static Boolean checkGreaterThan(Object value, Object minValue) {
        if(null == value) {
            return false;
        }
        if(value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof BigDecimal) {
            BigDecimal realValue = new BigDecimal(value.toString());
            BigDecimal realMin = new BigDecimal(minValue.toString());
            return realValue.compareTo(realMin)>=0;
        }else if(value instanceof Calendar) {
            Calendar realValue = (Calendar)value;
            Calendar realMin = (Calendar)minValue;
            return realValue.compareTo(realMin)>=0;
        } else if(value instanceof LocalDateTime) {
            LocalDateTime realValue = (LocalDateTime)value;
            LocalDateTime realMin = (LocalDateTime)minValue;
            return realValue.compareTo(realMin)>=0;
        }  else if(value instanceof LocalDate) {
            LocalDate realValue = (LocalDate)value;
            LocalDate realMin = (LocalDate)minValue;
            return realValue.compareTo(realMin)>=0;
        }
        return true;
    }
    /**
     * 验证传入参数是否“小于”传入最大值
     * @param value 参数值
     * @param maxValue 校验允许的最大值
     * @return 是否“小于”最大值
     */
    public static Boolean checkLess(Object value, Object maxValue) {
        if(null == value) {
            return true;
        }
        if(value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof BigDecimal) {
            BigDecimal realValue = new BigDecimal(value.toString());
            BigDecimal realMax = new BigDecimal(maxValue.toString());
            return realValue.compareTo(realMax)<0;
        } else if(value instanceof Calendar) {
            Calendar realValue = (Calendar)value;
            Calendar realMax = (Calendar)maxValue;
            return realValue.compareTo(realMax)<0;
        } else if(value instanceof LocalDateTime) {
            LocalDateTime realValue = (LocalDateTime)value;
            LocalDateTime realMax = (LocalDateTime)maxValue;
            return realValue.compareTo(realMax)<0;
        } else if(value instanceof LocalDate) {
            LocalDate realValue = (LocalDate)value;
            LocalDate realMax = (LocalDate)maxValue;
            return realValue.compareTo(realMax)<0;
        }
        return true;
    }
    /**
     * 验证传入参数是否“小于等于”传入最小值
     * @param value 参数值
     * @param maxValue 校验允许的最大值
     * @return 是否“小于等于”传入最小值
     */
    public static Boolean checkLessThan(Object value, Object maxValue) {
        if(null == value) {
            return true;
        }
        if(value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof BigDecimal) {
            BigDecimal realValue = new BigDecimal(value.toString());
            BigDecimal realMax = new BigDecimal(maxValue.toString());
            return realValue.compareTo(realMax)<=0;
        } else if(value instanceof Calendar) {
            Calendar realValue = (Calendar)value;
            Calendar realMax = (Calendar)maxValue;
            return realValue.compareTo(realMax)<=0;
        } else if(value instanceof LocalDateTime) {
            LocalDateTime realValue = (LocalDateTime)value;
            LocalDateTime realMax = (LocalDateTime)maxValue;
            return realValue.compareTo(realMax)<=0;
        } else if(value instanceof LocalDate) {
            LocalDate realValue = (LocalDate)value;
            LocalDate realMax = (LocalDate)maxValue;
            return realValue.compareTo(realMax)<=0;
        }
        return true;
    }
    /**
     * 验证传入参数是否符合指定“参数校验类型”格式
     * @param value 参数值
     * @param type 参数校验类型
     * @return 是否符合指定“参数校验类型”格式
     */
    public static Boolean checkIsMatch(Object value, EParameterValidateType type) {
        Boolean result = false;
        String realValue = String.valueOf(value);
        switch (type) {
            case MUST_DATE:
                result = StringUtil.isMatch(realValue, ConstantFactory.REGEX_DATE);
                break;
            case MUST_DATE_TIME:
                result = StringUtil.isMatch(realValue, ConstantFactory.REGEX_DATETIME);
                break;
            case MUST_EMAIL:
                result = StringUtil.isEmail(realValue);
                break;
            case MUST_MOBILE_NUMBER:
                result = StringUtil.isMobilePhone(realValue);
                break;
            case MUST_TELEPHONE_NUMBER:
                result = StringUtil.isTelPhone(realValue);
                break;
            case MUST_PHONE_NUMBER:
                result = (StringUtil.isTelPhone(realValue) || StringUtil.isMobilePhone(realValue));
                break;
                default:
                    break;
        }
        return result;
    }
    /**
     * 验证传入参数是否符合指定“参数校验类型”格式
     * @param regex 正则表达式
     * @param value 参数值
     * @return 是否符合指定“参数校验类型”格式
     */
    public static Boolean checkIsMatch(String regex, Object value) {
        return StringUtil.isMatch(String.valueOf(value), regex);
    }
    /**
     * 验证传入参数是否是指定类型的“有效枚举值”
     * @param value 参数值
     * @param enumType 枚举类型
     * @param <T> 枚举类型
     * @return 是否是“有效枚举值”
     */
    public static <T extends Enum<T>> Boolean chekIsEnum(Object value, Class<T> enumType) {
        if(value instanceof String) {
            return StringUtil.isEnum(enumType, value.toString());
        } else if(value instanceof Integer) {
            return NumericUtil.isEnum(enumType, ((Integer) value));
        }
        return false;
    }
    /**
     * 验证传入参数值是否在指定“最大值”和“最小值”范围内
     * @param value 参数值
     * @param minValue 校验允许的最大值
     * @param maxValue 校验允许的最小值
     * @return 是否在指定“最大值”和“最小值”范围内
     */
    public static Boolean checkInRange(Object value, Object minValue, Object maxValue) {
        if(null == value) {
            return false;
        }
        if(value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof BigDecimal) {
            BigDecimal realValue = new BigDecimal(value.toString());
            BigDecimal realMin = new BigDecimal(minValue.toString());
            BigDecimal realMax = new BigDecimal(maxValue.toString());
            return (realValue.compareTo(realMin)>=0 && realValue.compareTo(realMax)<=0);
        } else if(value instanceof Calendar) {
            Calendar realValue = (Calendar)value;
            Calendar realMin = (Calendar)minValue;
            Calendar realMax = (Calendar)maxValue;
            return (realValue.compareTo(realMin)>=0 && realValue.compareTo(realMax)<=0);
        }
        return true;
    }

    public ParameterValidator addEquals(String fieldName, Object value1, Object value2) {
        this.listParameter.add(new ParameterValidateItem(fieldName, EParameterValidateType.MUST_EQUALS, value1, value2, null, null));
        return this;
    }
}