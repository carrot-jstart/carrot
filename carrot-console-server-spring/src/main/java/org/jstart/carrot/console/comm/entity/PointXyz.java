package org.jstart.carrot.console.comm.entity;

import java.util.Objects;

public class PointXyz<M,T,V> {
    public M valueX;
    public T valueY;
    public V valueZ;

    public PointXyz(M valueX, T valueY, V valueZ) {
        this.valueX = valueX;
        this.valueY = valueY;
        this.valueZ = valueZ;
    }
    public PointXyz(){}

    @Override
    public String toString() {
        return "PointXyz{" +
                "valueX=" + valueX +
                ", valueY=" + valueY +
                ", valueZ=" + valueZ +
                '}';
    }

    public M getValueX() {
        return valueX;
    }
    public PointXyz<M,T,V> setValueX(M valueX) {
        this.valueX = valueX;
        return this;
    }

    public T getValueY() {
        return valueY;
    }
    public PointXyz<M,T,V> setValueY(T valueY) {
        this.valueY = valueY;
        return this;
    }

    public V getValueZ() {
        return valueZ;
    }
    public PointXyz<M,T,V> setValueZ(V valueZ) {
        this.valueZ = valueZ;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        PointXyz<?, ?, ?> pointXyz = (PointXyz<?, ?, ?>) object;
        return Objects.equals(valueX, pointXyz.valueX) && Objects.equals(valueY, pointXyz.valueY) && Objects.equals(valueZ, pointXyz.valueZ);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueX, valueY, valueZ);
    }


}
