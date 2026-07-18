package org.jstart.carrot.console.comm.lenum;


public enum EOderByType {

    CREATE_TIME(1,"crateTime默认ASC"),
    UPDATE_TIME(2,"updateTime默认ASC");

    private final Integer des;
    private final String value;

    EOderByType(Integer des, String value) {
        this.des = des;
        this.value = value;
    }

    public Integer getDes() {
        return des;
    }
    public String getValue() {
        return value;
    }
}
