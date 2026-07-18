package org.jstart.carrot.scheduling.constant;

public enum EJobUnitType {
    /**
     * 空
     */
    NONE(0),
    /**
     * 固定速度
     */
    FIXED_SPEED(1),
    /**
     * CORN 表达式
     */
    CORN(2),
    ;

    private final Integer value;

    private EJobUnitType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static EJobUnitType getByValue(Integer value) {
        for (EJobUnitType e : EJobUnitType.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }
        return EJobUnitType.NONE;
    }
}
