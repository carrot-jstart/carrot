package org.jstart.carrot.scheduling;


import org.jstart.carrot.scheduling.constant.EJobUnitType;

import java.io.Serializable;

public class SchedulingUnit implements Serializable {

    private String name;

    private EJobUnitType type;

    private String typeValue;

    public  SchedulingUnit(){}

    public SchedulingUnit(String name, EJobUnitType type, String typeValue) {
        this.name = name;
        this.type = type;
        this.typeValue = typeValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EJobUnitType getType() {
        return type;
    }

    public void setType(EJobUnitType type) {
        this.type = type;
    }

    public String getTypeValue() {
        return typeValue;
    }

    public void setTypeValue(String typeValue) {
        this.typeValue = typeValue;
    }
}
