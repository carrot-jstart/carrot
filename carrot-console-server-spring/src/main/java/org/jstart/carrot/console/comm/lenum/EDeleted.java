package org.jstart.carrot.console.comm.lenum;


public enum EDeleted {
    DELETED_NO(0,"未删除"),
    UPDATE_TIME(1,"已删除");

    private final Integer des;
    private final String value;

    EDeleted(Integer des, String value) {
        this.des = des;
        this.value = value;
    }

    public static EDeleted getValue(Integer des){
        for (EDeleted value : EDeleted.values()) {
            if(value.des.equals(des)){
                return value;
            }
        }
        return null;
    }

    public Integer getDes() {
        return des;
    }

    public String getValue() {
        return value;
    }
}
