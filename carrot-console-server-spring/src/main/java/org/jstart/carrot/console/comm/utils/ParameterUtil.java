package org.jstart.carrot.console.comm.utils;




import org.jstart.carrot.console.comm.ConstantFactory;
import org.jstart.carrot.console.comm.validator.Tuple;

import java.util.ArrayList;
import java.util.List;

public class ParameterUtil {
    public static String named(String name) {
        return "参数“" + name + "”";
    }

    public static String sqlASC(String name) {
        return name + ConstantFactory.SQL_ASC;
    }

    public static String sqlDESC(String name) {
        return name + ConstantFactory.SQL_DESC;
    }

    public static String sqlLikeValue(String value) {
        return "%" + value + "%";
    }

    public static String dealNull(String parameter) {
        if(StringUtil.isNullOrEmpty(parameter)) {
            return "";
        }
        return parameter.trim().replaceAll("\\t", "").replaceAll("\\r", "").replaceAll("\\n", "");
    }

    public static <T> List<T> dealNull(List<T> list) {
        if(null == list) {
            return new ArrayList<>();
        }
        return list;
    }

    public static String dealNotStr(String parameter) {
        if(null == parameter) {
            return parameter;
        }
        return parameter.trim().replaceAll("\\t", "").replaceAll("\\r", "").replaceAll("\\n", "");
    }

    public static Tuple<String, String> getTimeRange(String timeRange) {
        if (StringUtil.isNullOrEmpty(timeRange)) {
            return new Tuple<>("", "");
        }
        String[] list = StringUtil.split(timeRange, ConstantFactory.STR_TIME_RANGE);
        if (list.length != 2) {
            return new Tuple<>("", "");
        }
        return new Tuple<>(list[0], list[1]);
    }
}
