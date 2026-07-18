package org.jstart.carrot.console.comm.validator;

import java.util.function.Function;


public class ParameterValidateItem<T extends Enum> {
    private String fieldName;
    private EParameterValidateType type;
    private Object value;
    private Object rangeMin;
    private Object rangeMax;
    private Class<T> enumType;
    private Function<Object, Boolean> validateFunc = null;

    public ParameterValidateItem() {}
    public ParameterValidateItem(String fieldName, EParameterValidateType type, Object value) {
        this.fieldName = fieldName;
        this.type = type;
        this.value = value;
    }
    public ParameterValidateItem(String fieldName, EParameterValidateType type, Object value, Function<Object, Boolean> validateFunc) {
        this.fieldName = fieldName;
        this.type = type;
        this.value = value;
        this.validateFunc = validateFunc;
    }
    public ParameterValidateItem(String fieldName, EParameterValidateType type, Object value, Object rangeMin, Object rangeMax) {
        this.fieldName = fieldName;
        this.type = type;
        this.value = value;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
    }
    public ParameterValidateItem(String fieldName, EParameterValidateType type, Object value, Object rangeMin, Object rangeMax, Class<T> enumType) {
        this.fieldName = fieldName;
        this.type = type;
        this.value = value;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.enumType = enumType;
    }
    public ParameterValidateItem(String fieldName, Object value, Object rangeMin, Object rangeMax) {
        this.fieldName = fieldName;
        this.type = EParameterValidateType.MUST_IN_RANGE;
        this.value = value;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
    }
    public ParameterValidateItem(String fieldName, Object value , Class<T> enumType) {
        this.fieldName = fieldName;
        this.type = EParameterValidateType.MUST_ENUM;
        this.value = value;
        this.enumType = enumType;
    }

    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public EParameterValidateType getType() {
        return type;
    }
    public void setType(EParameterValidateType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    public Object getRangeMin() {
        return rangeMin;
    }
    public void setRangeMin(Object rangeMin) {
        this.rangeMin = rangeMin;
    }

    public Object getRangeMax() {
        return rangeMax;
    }
    public void setRangeMax(Object rangeMax) {
        this.rangeMax = rangeMax;
    }

    public Class<T> getEnumType() {
        return enumType;
    }
    public void setEnumType(Class<T> enumType) {
        this.enumType = enumType;
    }

    public Function<Object, Boolean> getValidateFunc() {
        return validateFunc;
    }
    public void setValidateFunc(Function<Object, Boolean> validateFunc) {
        this.validateFunc = validateFunc;
    }
}