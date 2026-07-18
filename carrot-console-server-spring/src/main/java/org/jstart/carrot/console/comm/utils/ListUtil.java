package org.jstart.carrot.console.comm.utils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * List工具类
 * @author carrot
 * @date   2024-5-11
 */
public final class ListUtil {
    private ListUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * array转list
     * @param array 数组
     * @param <T> 实体类型
     */
    public static <T> List<T> toList(T... array) {
        if(array==null || array.length==0) {
            return new ArrayList<>();
        }
        return Arrays.stream(array).collect(Collectors.toList());
    }

    /**
     * Long类型 list转array
     * @param list 集合
     */
    public static Long[] toLongArray(List<Long> list) {
        if(ListUtil.isNullOrEmpty(list)) {
            return new Long[0];
        }
        Long[] array = new Long[list.size()];
        return list.toArray(array);
    }
    /**
     * Integer类型 list转array
     * @param list 集合
     */
    public static Integer[] toIntArray(List<Integer> list) {
        if(ListUtil.isNullOrEmpty(list)) {
            return new Integer[0];
        }
        Integer[] array = new Integer[list.size()];
        return list.toArray(array);
    }
    /**
     * BigDecimal类型 list转array
     * @param list 集合
     */
    public static BigDecimal[] toDecimalArray(List<BigDecimal> list) {
        if(ListUtil.isNullOrEmpty(list)) {
            return new BigDecimal[0];
        }
        BigDecimal[] array = new BigDecimal[list.size()];
        return list.toArray(array);
    }
    /**
     * Double类型 list转array
     * @param list 集合
     */
    public static Double[] toDoubleArray(List<Double> list) {
        if(ListUtil.isNullOrEmpty(list)) {
            return new Double[0];
        }
        Double[] array = new Double[list.size()];
        return list.toArray(array);
    }
    /**
     * String类型 list转array
     * @param list 集合
     */
    public static String[] toArray(List<String> list) {
        if(ListUtil.isNullOrEmpty(list)) {
            return new String[0];
        }
        String[] array = new String[list.size()];
        return list.toArray(array);
    }

    /**
     * 判断集合【为】null或empty
     * @param list 集合
     * @param <T> 集合实体类型
     */
    public static <T> boolean isNullOrEmpty(Collection<T> list){
        return list==null || list.isEmpty();
    }

    /**
     * 判断集合【不为】null或empty
     * @param list 集合
     * @param <T> 集合实体类型
     */
    public static <T> boolean isNotNullOrEmpty(Collection<T> list){
        return !ListUtil.isNullOrEmpty(list);
    }

    /**
     * String类型集合转Integer类型集合
     * @param list 集合
     */
    public static List<Integer> toIntList(List<String> list) {
        if(ListUtil.isNullOrEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    /**
     * String类型集合转Long类型集合
     * @param list 集合
     */
    public static List<Long> toLongList(List<String> list) {
        if(ListUtil.isNullOrEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(Long::parseLong).collect(Collectors.toList());
    }

    /**
     * Integer类型集合转String类型集合
     * @param list 集合
     */
    public static List<String> toStringList(List<Integer> list) {
        if(ListUtil.isNullOrEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(String::valueOf).collect(Collectors.toList());
    }

    /**
     * BigDecimal类型集合求和
     * @param list 集合
     */
    public static BigDecimal sum(Collection<BigDecimal> list) {
        BigDecimal result = BigDecimal.ZERO;
        if(ListUtil.isNotNullOrEmpty(list)) {
            result = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return result;
    }
    /**
     * 判断集合非空后，执行委托
     * @param list 集合
     * @param predicate 循环集合的委托代码
     * @param <T> 集合实体类型
     * @param <R> 函数(集合委托)返回值，委托里最后一句写死“return null;”
     */
    public static <T, R> void notNullForeach(Collection<T> list, Function<T, R> predicate) {
        if(ListUtil.isNotNullOrEmpty(list)) {
            for (T item : list) {
                predicate.apply(item);
            }
        }
    }

    /**
     * 集合去重
     * @param list 集合
     * @param <T> 集合实体类型
     */
    public static <T> void repeat(Collection<T> list) {
        HashSet<T> hash = new HashSet<T>(list);
        list.clear();
        list.addAll(hash);
    }

    /**
     * 判断集合是否存在重复元素
     * @param list 集合
     * @param <T> 集合实体类型
     */
    public static <T> Boolean hasRepeat(Collection<T> list) {
        int oldSize = list.size();
        repeat(list);
        return list.size()!=oldSize;
    }

    /**
     * 集合非空，则执行委托
     * @param list 集合
     * @param predicate 循环集合的委托代码
     * @param <T> 集合实体类型
     */
    public static <T> void notNullForeach(Collection<T> list, Consumer<T> predicate) {
        if(ListUtil.isNotNullOrEmpty(list)) {
            for (T item : list) {
                predicate.accept(item);
            }
        }
    }
}