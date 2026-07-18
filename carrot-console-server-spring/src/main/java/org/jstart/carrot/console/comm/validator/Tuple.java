package org.jstart.carrot.console.comm.validator;

/**
 * 2个泛型参数返回结果
 * @param <T1> 参数1的类型
 * @param <T2> 参数2的类型
 */
public class Tuple<T1, T2> {
    private T1 item1;
    private T2 item2;

    public Tuple() {
    }

    public Tuple(T1 item1, T2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public T1 getItem1() {
        return this.item1;
    }

    public void setItem1(T1 item1) {
        this.item1 = item1;
    }

    public T2 getItem2() {
        return this.item2;
    }

    public void setItem2(T2 item2) {
        this.item2 = item2;
    }
}